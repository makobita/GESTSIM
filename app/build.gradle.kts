plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// número de versión automático: en GitHub es el número de compilación (1, 2, 3...)
val buildNum = (System.getenv("GITHUB_RUN_NUMBER") ?: "1").toInt()

android {
    namespace = "com.makoba.urano"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.makoba.urano"
        minSdk = 26
        targetSdk = 35
        versionCode = buildNum
        versionName = "1.0.$buildNum"
    }

    signingConfigs {
        // SIEMPRE la misma firma: si cambia, el móvil no deja actualizar encima
        create("gestsim") {
            storeFile = file(System.getenv("KEYSTORE_FILE") ?: "../keystore/gestsim.jks")
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: "gestsim2026"
            keyAlias = System.getenv("KEY_ALIAS") ?: "gestsim"
            keyPassword = System.getenv("KEY_PASSWORD") ?: "gestsim2026"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("gestsim")
        }
        debug {
            signingConfig = signingConfigs.getByName("gestsim")
        }
    }
    buildFeatures {
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    androidResources {
        noCompress += listOf("html", "js")
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.webkit:webkit:1.12.1")
}
