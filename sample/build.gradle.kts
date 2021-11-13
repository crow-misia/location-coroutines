import de.fayard.refreshVersions.core.versionFor

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
}

android {
    compileSdk = 31

    defaultConfig {
        applicationId = "com.example.sample"
        minSdk = 23
        targetSdk = 31
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict", "-Xopt-in=kotlin.RequiresOptIn")
        jvmTarget = "11"
        apiVersion = "1.5"
        languageVersion = "1.5"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = versionFor(AndroidX.compose.compiler)
    }
    packagingOptions {
        resources {
            excludes.add("/META-INF/{AL2.0,LGPL2.1}")
        }
    }
}

dependencies {
    implementation(project(":location-coroutines"))

    implementation(Kotlin.stdlib)

    implementation(AndroidX.activity.compose)
    implementation(AndroidX.appCompat)
    implementation(AndroidX.core.ktx)

    implementation(AndroidX.compose.ui)
    implementation(AndroidX.compose.ui.toolingPreview)
    implementation(AndroidX.compose.material)
    implementation(AndroidX.compose.runtime.liveData)
    implementation(AndroidX.lifecycle.runtimeKtx)
    implementation(AndroidX.lifecycle.viewModelCompose)
    implementation(AndroidX.navigation.compose)
    implementation(Google.android.material)

    implementation(JakeWharton.timber)

    implementation("com.google.accompanist:accompanist-permissions:_")

    testImplementation(Testing.junit4)
    androidTestImplementation(AndroidX.test.ext.junitKtx)
    androidTestImplementation(AndroidX.test.espresso.core)
    androidTestImplementation(AndroidX.compose.ui.testJunit4)

    debugImplementation(AndroidX.compose.ui.tooling)
}
