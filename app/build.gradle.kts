plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.jack.web"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.jack.web"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        // 核心配置：只打包 arm64-v8a 架构
        ndk {
            abiFilters.add("arm64-v8a")
        }
    }

    buildTypes {
        release {
            // 启用代码混淆和资源压缩进一步减小体积
            isMinifyEnabled = true
            isShrinkResources = true
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

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("com.google.android.material:material:1.11.0") // 添加这一行
}
