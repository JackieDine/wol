plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.qqwebview"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.qqwebview"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true   // 开启代码混淆
            isShrinkResources = true // 开启资源缩减
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}

dependencies {
    // 仅保留最基础的核心库，删除 Material 和 ConstraintLayout 以减小体积
    implementation("androidx.appcompat:appcompat:1.6.1")
}
