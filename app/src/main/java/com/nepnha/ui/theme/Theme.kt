package com.nepnha.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * Theme của app.
 *
 * **Cố ý KHÔNG dùng dynamic color (Material You).** Nếp Nhà là một app nghi lễ, cần
 * giữ giọng màu nhất quán và trang trọng; để hệ thống nhuộm màu theo hình nền sẽ
 * làm giao diện khi thì tím, khi thì xanh — vừa mất bản sắc vừa khó bảo đảm tương
 * phản chữ cho người lớn tuổi.
 */
private val LightColors = lightColorScheme(
    primary = BrandRed,
    onPrimary = SurfaceLight,
    primaryContainer = BrandRedContainer,
    onPrimaryContainer = OnBrandRedContainer,
    secondary = Bronze,
    onSecondary = SurfaceLight,
    secondaryContainer = BronzeContainer,
    onSecondaryContainer = OnBronzeContainer,
    tertiary = Bronze,
    onTertiary = SurfaceLight,
    tertiaryContainer = BronzeContainer,
    onTertiaryContainer = OnBronzeContainer,
    background = PaperLight,
    onBackground = InkLight,
    surface = SurfaceLight,
    onSurface = InkLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = InkMutedLight,
    surfaceContainerLowest = SurfaceContainerLowestLight,
    surfaceContainerLow = SurfaceContainerLowLight,
    surfaceContainer = SurfaceContainerLight,
    surfaceContainerHigh = SurfaceContainerHighLight,
    surfaceContainerHighest = SurfaceContainerHighestLight,
    surfaceBright = PaperLight,
    surfaceDim = SurfaceDimLight,
    inverseSurface = InkLight,
    inverseOnSurface = PaperLight,
    inversePrimary = BrandRedLight,
    outline = OutlineLight,
    outlineVariant = OutlineLight,
)

private val DarkColors = darkColorScheme(
    primary = BrandRedLight,
    onPrimary = PaperDark,
    primaryContainer = OnBrandRedContainer,
    onPrimaryContainer = BrandRedContainer,
    secondary = BronzeContainer,
    onSecondary = PaperDark,
    secondaryContainer = OnBronzeContainer,
    onSecondaryContainer = BronzeContainer,
    tertiary = BronzeContainer,
    onTertiary = PaperDark,
    tertiaryContainer = OnBronzeContainer,
    onTertiaryContainer = BronzeContainer,
    background = PaperDark,
    onBackground = InkDark,
    surface = SurfaceDark,
    onSurface = InkDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = InkMutedDark,
    surfaceContainerLowest = SurfaceContainerLowestDark,
    surfaceContainerLow = SurfaceContainerLowDark,
    surfaceContainer = SurfaceContainerDark,
    surfaceContainerHigh = SurfaceContainerHighDark,
    surfaceContainerHighest = SurfaceContainerHighestDark,
    surfaceBright = SurfaceContainerHighestDark,
    surfaceDim = SurfaceDimDark,
    inverseSurface = InkDark,
    inverseOnSurface = PaperDark,
    inversePrimary = BrandRed,
    outline = OutlineDark,
    outlineVariant = OutlineDark,
)

@Composable
fun NepNhaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = NepNhaTypography,
        content = content,
    )
}
