plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "ru.superplushkin.twofactorauthapp"
    compileSdk = 37

    defaultConfig {
        applicationId = "ru.superplushkin.twofactorauthapp"
        minSdk = 24
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    packaging {
        jniLibs {
            useLegacyPackaging = false
        }
        resources {
            excludes += "**/dump_syms.bin" // там баг с 4kb библиотекой по отладке
        }
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.firebase.crashlytics.buildtools)

    implementation(libs.commons.codec)
    implementation(libs.gson)

    implementation(libs.kotlin.onetimepassword)

    implementation(libs.core)
    implementation(libs.barcode.scanning)

    implementation(libs.camera.view)
    implementation(libs.camera.lifecycle)
    implementation(libs.camera.core)
    implementation(libs.androidx.camera.camera2)

    implementation(libs.slidableactivity)
}