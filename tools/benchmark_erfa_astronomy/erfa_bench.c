/*
 * Nếp Nhà — công cụ benchmark thiên văn. KHÔNG phải code của app.
 *
 * Nằm ngoài app/, viết bằng C, không bao giờ được build vào APK.
 * Mục đích duy nhất: đo xem ERFA có đủ chính xác để xác định NGÀY ÂM VIỆT NAM
 * hay không, trước khi quyết định kiến trúc engine.
 *
 * Thư viện: ERFA (Essential Routines for Fundamental Astronomy), BSD-3-Clause,
 * phái sinh có phép từ IAU SOFA. Xem docs/ASTRONOMICAL_PROVENANCE.md.
 *
 * Biên dịch:
 *   cc -O2 -I<erfa-src> erfa_bench.c liberfa.a -lm -o erfa_bench
 *
 * Dùng:
 *   ./erfa_bench newmoon <deltaT_seconds> <jdTT_guess>
 *   ./erfa_bench sunlong <deltaT_seconds> <target_degrees> <jdTT_guess>
 *   ./erfa_bench batch                      # stdin: "deltaT jdTT_guess" mỗi dòng
 */
#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <string.h>
#include "erfa.h"
#include "erfam.h"

/* Hoàng kinh biểu kiến của Mặt Trời, tính tại thời điểm TT. Đơn vị: radian.
 * "Biểu kiến" = đã hiệu chỉnh thời gian truyền sáng (quang sai) và chương động —
 * đúng định nghĩa mà quy tắc trung khí dùng. Bỏ hai hiệu chỉnh này sẽ lệch tới
 * ~15 phút thời gian, đủ để sai ngày. */
static double sun_apparent_lon(double tt1, double tt2)
{
    double pvh[2][3], pvb[2][3], s[3], r, tau, dpsi, deps, ra, dec, dl, db;

    eraEpv00(tt1, tt2, pvh, pvb);
    s[0] = -pvh[0][0]; s[1] = -pvh[0][1]; s[2] = -pvh[0][2];
    r = sqrt(s[0]*s[0] + s[1]*s[1] + s[2]*s[2]);
    tau = r * ERFA_AULT / ERFA_DAYSEC;              /* thời gian truyền sáng, ngày */

    eraEpv00(tt1, tt2 - tau, pvh, pvb);             /* lùi lại theo thời gian truyền sáng */
    s[0] = -pvh[0][0]; s[1] = -pvh[0][1]; s[2] = -pvh[0][2];

    eraC2s(s, &ra, &dec);
    eraEqec06(tt1, tt2, eraAnp(ra), dec, &dl, &db); /* → hoàng đạo trung bình của ngày */
    eraNut06a(tt1, tt2, &dpsi, &deps);
    return eraAnp(dl + dpsi);                       /* + chương động = biểu kiến */
}

/* Hoàng kinh biểu kiến của Mặt Trăng (địa tâm). Đơn vị: radian. */
static double moon_apparent_lon(double tt1, double tt2)
{
    double pv[2][3], dpsi, deps, ra, dec, dl, db;

    eraMoon98(tt1, tt2, pv);
    eraC2s(pv[0], &ra, &dec);
    eraEqec06(tt1, tt2, eraAnp(ra), dec, &dl, &db);
    eraNut06a(tt1, tt2, &dpsi, &deps);
    return eraAnp(dl + dpsi);
}

/* Hiệu kinh độ Trăng − Trời, quy về (−180°, +180°]. Bằng 0 tại điểm Sóc. */
static double elongation(double tt1, double tt2)
{
    return eraAnpm(moon_apparent_lon(tt1, tt2) - sun_apparent_lon(tt1, tt2));
}

/* Tìm nghiệm bằng phương pháp dây cung. Trả về JD(TT) của điểm Sóc. */
static double solve_newmoon(double jd_guess)
{
    double t0 = jd_guess - 0.5, t1 = jd_guess + 0.5, f0, f1, t2;
    int i;
    f0 = elongation(t0, 0.0);
    f1 = elongation(t1, 0.0);
    for (i = 0; i < 60; i++) {
        if (fabs(f1 - f0) < 1e-14) break;
        t2 = t1 - f1 * (t1 - t0) / (f1 - f0);
        t0 = t1; f0 = f1; t1 = t2; f1 = elongation(t1, 0.0);
        if (fabs(f1) < 1e-12) break;
    }
    return t1;
}

/* Tìm thời điểm hoàng kinh Mặt Trời bằng target (radian). */
static double solve_sunlon(double target, double jd_guess)
{
    double t0 = jd_guess - 2.0, t1 = jd_guess + 2.0, f0, f1, t2;
    int i;
    f0 = eraAnpm(sun_apparent_lon(t0, 0.0) - target);
    f1 = eraAnpm(sun_apparent_lon(t1, 0.0) - target);
    for (i = 0; i < 60; i++) {
        if (fabs(f1 - f0) < 1e-14) break;
        t2 = t1 - f1 * (t1 - t0) / (f1 - f0);
        t0 = t1; f0 = f1; t1 = t2; f1 = eraAnpm(sun_apparent_lon(t1, 0.0) - target);
        if (fabs(f1) < 1e-12) break;
    }
    return t1;
}

/* In JD(UT) ra lịch + giờ. deltaT giây: UT = TT − ΔT. */
static void print_ut(double jd_tt, double deltaT_sec)
{
    double jd_ut = jd_tt - deltaT_sec / ERFA_DAYSEC;
    int iy, im, id, ihmsf[4];
    if (eraD2dtf("UTC", 3, jd_ut, 0.0, &iy, &im, &id, ihmsf) < 0) { printf("ERR"); return; }
    printf("%04d-%02d-%02d %02d:%02d:%02d.%03d",
           iy, im, id, ihmsf[0], ihmsf[1], ihmsf[2], ihmsf[3]);
}

int main(int argc, char **argv)
{
    if (argc < 2) { fprintf(stderr, "xem chú thích đầu file\n"); return 2; }
    if (strcmp(argv[1], "batch") && strcmp(argv[1], "sunbatch") && argc < 4) {
        fprintf(stderr, "thiếu tham số\n"); return 2;
    }

    if (!strcmp(argv[1], "newmoon")) {
        double dt = atof(argv[2]), guess = atof(argv[3]);
        double jd = solve_newmoon(guess);
        printf("jdTT=%.9f  UT=", jd); print_ut(jd, dt);
        printf("  residual_arcsec=%.4f\n", elongation(jd, 0.0) * ERFA_DR2AS);
    } else if (!strcmp(argv[1], "sunlong")) {
        double dt = atof(argv[2]), deg = atof(argv[3]), guess = atof(argv[4]);
        double jd = solve_sunlon(deg * ERFA_DD2R, guess);
        printf("jdTT=%.9f  UT=", jd); print_ut(jd, dt);
        printf("  lon=%.6f deg\n", sun_apparent_lon(jd, 0.0) * ERFA_DR2D);
    } else if (!strcmp(argv[1], "sunbatch")) {
        /* stdin: "deltaT target_deg jdTT_guess" mỗi dòng.
           Chỉ dùng eraEpv00 — nhánh Mặt Trời, KHÔNG dính Meeus. */
        double dt, deg, guess;
        while (scanf("%lf %lf %lf", &dt, &deg, &guess) == 3) {
            double jd = solve_sunlon(deg * ERFA_DD2R, guess);
            printf("%.9f %.6f %.9f\n", jd, dt,
                   sun_apparent_lon(jd, 0.0) * ERFA_DR2D);
        }
    } else if (!strcmp(argv[1], "batch")) {
        /* Đọc từ stdin các dòng "deltaT jdTT_guess", in ra JD(TT) của điểm Sóc.
           Chạy hàng loạt trong một tiến trình để quét cả 1901-2100 cho nhanh. */
        double dt, guess;
        while (scanf("%lf %lf", &dt, &guess) == 2) {
            double jd = solve_newmoon(guess);
            printf("%.9f %.6f\n", jd, dt);
        }
    } else { fprintf(stderr, "lệnh không hợp lệ\n"); return 2; }
    return 0;
}
