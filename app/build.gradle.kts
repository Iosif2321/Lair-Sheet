plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")        // обязателен для Kotlin 2.0+
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.example.lairsheet"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.lairsheet"
        minSdk = 24
        targetSdk = 34
        versionCode = 3
        versionName = "0.0.3"
        vectorDrawables.useSupportLibrary = true
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    // Единый таргет на JDK 17 (Gradle сам подтянет её через Foojay toolchains)
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // В Kotlin 2.0+ версию compose-compiler НЕ указываем (её настраивает plugin compose)
    packaging {
        resources.excludes += setOf("META-INF/*")
    }
}

kotlin {
    // Для Kotlin и KSP
    jvmToolchain(17)
}

dependencies {
    // Compose BOM + базовые артефакты
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // ⬇️ важно: даёт KeyboardOptions и остальной текстовый стек
    implementation("androidx.compose.ui:ui-text")

    // Навигация/активити
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // AndroidX
    implementation("androidx.core:core-ktx:1.13.1")

    // Room + KSP
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // Kotlinx Serialization (для загрузки правил из assets)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
}
