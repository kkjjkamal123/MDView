plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "app.mdview"
    compileSdk = 36

    defaultConfig {
        applicationId = "app.mdview"
        minSdk = 26
        targetSdk = 36
        versionCode = 10200
        versionName = "1.2.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            // Signed with the debug key so the APK installs by sideloading; swap in a real key for the Play Store.
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    // The rendering engine is shared with the Mac app: its Resources folder is used as-is.
    sourceSets.getByName("main").assets.srcDir("../../MDView/Resources")

    androidResources {
        // Fonts and scripts are read as-is; compressing them only costs time on every read.
        noCompress += listOf("js", "css", "woff2")
    }
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.webkit)
    implementation(libs.compose.ui)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.compose.icons)
    implementation(libs.compose.tooling.preview)
    debugImplementation(libs.compose.tooling)
    testImplementation(libs.junit)
}
