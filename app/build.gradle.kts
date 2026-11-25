import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.0"

    kotlin("kapt")
}

android {
    namespace = "dev.atharvkhunte.docvault"
    compileSdk = 36

    defaultConfig {
        applicationId = "dev.atharvkhunte.docvault"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    signingConfigs {
        create("release") {
            storeFile = file("../docvault-release.jks")
            storePassword = "Antara@2527"
            keyAlias = "docvault"
            keyPassword = "Antara@2527"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17) // Use JVM_11 or JVM_21 depending on your Java target
        }
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    // === Supabase Cor e (Pinned to v3) ===


    // === Core Android ===
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

    // === Jetpack Compose ===
    implementation(platform(libs.androidx.compose.bom))

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

// Compose testing and debugging
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // === Navigation ===
    implementation("androidx.navigation:navigation-compose:2.7.5")

    // === CameraX Integration ===
    implementation("androidx.camera:camera-core:1.3.0")
    implementation("androidx.camera:camera-camera2:1.3.0")
    implementation("androidx.camera:camera-lifecycle:1.3.0")
    implementation("androidx.camera:camera-view:1.3.0")
    implementation("androidx.compose.material:material-icons-extended")
    implementation(libs.coil.compose)
    implementation("com.google.accompanist:accompanist-permissions:0.34.0")

    // === ML Kit & Document Scanner ===
    implementation("com.google.mlkit:text-recognition:16.0.0")
    implementation("com.google.android.gms:play-services-mlkit-document-scanner:16.0.0-beta1")

    // === Encryption & Database Store ===
    implementation("androidx.room:room-runtime:2.7.0")
    implementation("androidx.room:room-ktx:2.7.0")
    kapt("androidx.room:room-compiler:2.7.0")
    implementation("net.zetetic:android-database-sqlcipher:4.5.4")
    implementation("androidx.sqlite:sqlite-ktx:2.4.0")
    implementation("com.google.crypto.tink:tink-android:1.10.0")

    // === Asynchronous Coroutines ===
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")

    // === UI Enhancements & Essentials ===
    implementation("dev.chrisbanes.haze:haze:0.4.1")
    implementation("androidx.biometric:biometric:1.1.0")
    implementation("androidx.core:core-splashscreen:1.0.1")

    // === Ktor Networking Client Engines ===
    implementation("io.ktor:ktor-client-android:3.0.0")
    implementation("io.ktor:ktor-client-core:3.0.0")
    // === Session Key Persistence ===
    implementation("com.russhwolf:multiplatform-settings:1.1.1")
    implementation("com.russhwolf:multiplatform-settings-no-arg:1.1.1")

    // === Kotlin Serialization Engine ===
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    // === Native Google Sign-In Integrations ===
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation(libs.googleid)
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")

    // === Test Frameworks ===
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.6.0")
    debugImplementation("androidx.compose.ui:ui-tooling:1.6.0")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.6.0")
}
