plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    kotlin("kapt")
//    id("com.google.devtools.ksp") version "1.9.0"
}

android {
    namespace = "com.jve386.kpopchunichi_bcreader"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.jve386.kpopchunichi_bcreader"
        minSdk = 30
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
//    kapt {
//        correctErrorTypes = true
//        useBuildCache = true // Optional: enable build cache for kapt
//        javacOptions {
//            // Specify the Java version here
//            option("target", "1.8")
//        }
//    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.jsoup)
    implementation(libs.androidx.appcompat)
//    implementation("com.google.android.material:material:1.12.0")
//    implementation("com.github.bumptech.glide:glide:4.15.1")
    kapt("com.github.bumptech.glide:compiler:4.15.1")
//    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Dependencias actualizadas
    implementation(libs.material)
    implementation(libs.glide)
    annotationProcessor ("com.github.bumptech.glide:compiler:4.15.1")
//    ksp(libs.glide.compiler)  // Cambiado de kapt a ksp
    implementation(libs.coroutines.android)
    implementation ("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation("org.apache.poi:poi-ooxml:5.2.4")

}