plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    kotlin("plugin.serialization")
}

android {
    namespace = "com.zeros.basheer"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.zeros.basheer"
        minSdk = 26  // Good choice - covers 98%+ devices
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0-alpha"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Add this for vector drawables support
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true  // CHANGED: Enable minification for smaller APK
            isShrinkResources = true  // ADDED: Remove unused resources
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17  // CHANGED: Use Java 17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"  // CHANGED: Use Java 17
    }

    buildFeatures {
        compose = true
    }

    // ADDED: For proper packaging
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    implementation(libs.bundles.compose)  // CHANGED: Use bundle

    // Navigation - ADDED
    implementation(libs.androidx.navigation.compose)

    // Room Database - ADDED
    implementation(libs.bundles.room)
    ksp(libs.androidx.room.compiler)

    // Hilt Dependency Injection - ADDED
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // DataStore - ADDED
    implementation(libs.androidx.datastore.preferences)

    // Coil for image loading - ADDED
    implementation(libs.coil.compose)

    // Coroutines - ADDED
    implementation(libs.kotlinx.coroutines.android)

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // gson
    implementation("com.google.code.gson:gson:2.10.1")




    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(composeBom)
    androidTestImplementation(libs.androidx.ui.test.junit4)

    // Debug - CHANGED: Use bundle
    debugImplementation(libs.bundles.compose.debug)
}