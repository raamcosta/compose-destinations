plugins {
    id("com.android.application")
    kotlin("android")
    id("com.google.devtools.ksp") version libs.versions.ksp.get()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions.freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
}

android {
    namespace = "com.ramcosta.destinations.sample.wear"
    compileSdk = libs.versions.compileSdk.get().toIntOrNull()

    defaultConfig {
        applicationId = "com.ramcosta.destinations.sample.wear"
        minSdk = 25
        targetSdk = libs.versions.targetSdk.get().toIntOrNull()
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
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }

    packaging {
        resources.excludes.add("/META-INF/{AL2.0,LGPL2.1}")
    }
}

dependencies {
    implementation(project(mapOf("path" to ":compose-destinations-wear")))
    ksp(project(":compose-destinations-ksp"))

    implementation(libs.androidMaterial)

    implementation(libs.compose.ui)
    implementation(libs.compose.material)
    implementation(libs.compose.viewModel)

    implementation(libs.androidx.lifecycleRuntimeKtx)
    implementation(libs.androidx.activityCompose)

    implementation(libs.wear.compose.foundation)
    implementation(libs.wear.compose.material)
    implementation(libs.wear.input)
}
