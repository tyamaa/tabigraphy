plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }

    sourceSets {
        commonMain.dependencies {
            // `Flow` is part of LocationTracker's public API, so this needs to be
            // `api` (not `implementation`) to be visible to consumers like :composeApp.
            api(libs.kotlinx.coroutines.core)
        }
        androidMain.dependencies {
            implementation(libs.play.services.location)
            implementation(libs.androidx.core.ktx)
        }
    }
}

android {
    namespace = "com.tabigraphy.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
