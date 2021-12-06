plugins {
    id("com.android.application")
    id("kotlin-android")
    id("com.google.devtools.ksp") version Versions.ksp
}

kotlin {
    sourceSets {
        debug {
            kotlin.srcDir("build/generated/ksp/debug/kotlin")
        }
        release {
            kotlin.srcDir("build/generated/ksp/release/kotlin")
        }
    }
}

android {
    compileSdk = Versions.compileSdk

    defaultConfig {
        applicationId = "com.ramcosta.samples.destinationstodosample"
        minSdk = Versions.minSdk
        targetSdk = Versions.targetSdk
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = Versions.compose
    }

    packagingOptions {
        resources.excludes.add("/META-INF/{AL2.0,LGPL2.1}")
    }

    // TO turn off navigation graphs generation:
    // (if you prefer creating NavGraph instances or you prefer using the normal NavHost)
//    ksp {
//        arg("compose-destinations.generateNavGraphs", "false")
//    }
}

dependencies {

    implementation(project(mapOf("path" to ":compose-destinations-animations")))
    ksp(project(":compose-destinations-ksp"))

    with(Deps.Android) {
        implementation(material)
    }

    with(Deps.Compose) {
        implementation(ui)
        implementation(material)
        implementation(viewModel)
    }

    with(Deps.AndroidX) {
        implementation(lifecycleRuntimeKtx)
        implementation(activityCompose)
    }
}