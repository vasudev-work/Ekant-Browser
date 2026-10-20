plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.vasudev.ekant.gecko"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.vasudev.ekant.gecko"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "0.1-spike"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    // Resolved by CI (see .github/workflows/build-geckoview-spike.yml)
    val geckoVersion = System.getenv("GECKOVIEW_VERSION") ?: "125.0.20240321090858"
    implementation("org.mozilla.geckoview:geckoview-omni:$geckoVersion")
}
