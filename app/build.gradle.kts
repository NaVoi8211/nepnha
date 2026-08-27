import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

/**
 * Thông tin ký bản phát hành đọc từ `keystore.properties` ở gốc dự án — một file
 * **không bao giờ được commit** (xem `.gitignore`).
 *
 * Cố ý không có giá trị mặc định và không có keystore nào nằm trong repo: keystore là
 * tài sản của chủ dự án. Không có file thì bản release vẫn build được nhưng **không
 * được ký**, và Gradle nói thẳng điều đó thay vì lặng lẽ ký bằng debug key rồi để chủ
 * dự án tải một file vô dụng lên Play.
 */
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        keystorePropertiesFile.inputStream().use { load(it) }
    }
}

/** Thiếu khoá nào thì fail ngay, kèm tên khoá — không bao giờ in giá trị. */
fun requiredKeystoreProperty(name: String): String =
    keystoreProperties.getProperty(name)?.takeIf { it.isNotBlank() }
        ?: throw GradleException(
            "keystore.properties thiếu '$name'. Cần đủ: storeFile, storePassword, " +
                "keyAlias, keyPassword. Xem docs/PHASE_7_5_RELEASE_AUDIT.md.",
        )


plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.nepnha"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.nepnha"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "0.1.0-mvp"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // Không dùng support-library vector => vector được render native (API 21+).
        vectorDrawables.useSupportLibrary = false
    }

    signingConfigs {
        if (keystorePropertiesFile.exists()) {
            create("release") {
                storeFile = rootProject.file(requiredKeystoreProperty("storeFile"))
                storePassword = requiredKeystoreProperty("storePassword")
                keyAlias = requiredKeystoreProperty("keyAlias")
                keyPassword = requiredKeystoreProperty("keyPassword")
                // Play App Signing nhận cả hai; bật đủ để APK cài trực tiếp cũng hợp lệ.
                enableV1Signing = true
                enableV2Signing = true
            }
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
        }
        release {
            signingConfig = signingConfigs.findByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    buildFeatures {
        compose = true
        // Tắt hết những gì không dùng => ít task, build nhanh hơn trên máy 8GB.
        buildConfig = false
        aidl = false
        shaders = false
        resValues = false
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            // Bảng dò của trình gỡ lỗi coroutine. Chỉ có tác dụng khi cài debug agent,
            // nên với người dùng cuối nó là 1,7 KB chết nằm trong mọi bản cài.
            excludes += "DebugProbesKt.bin"
        }
    }
}

kotlin {
    compilerOptions {
        // Không dùng jvmToolchain(17): máy chỉ có JDK 25 (JBR), toolchain 17 sẽ
        // phải auto-download JDK => cần mạng. Biên dịch bằng JDK 25, target 17.
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

ksp {
    // Room sẽ export schema JSON ra app/schemas để review migration ở Phase 2+.
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)

    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    implementation(libs.androidx.navigation.compose)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.androidx.datastore.preferences)

    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)

    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.room.testing)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
