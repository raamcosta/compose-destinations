plugins {
    id("com.android.application")
    id("kotlin-android")
    id("com.google.devtools.ksp") version Versions.ksp
    id("kotlin-parcelize")
    kotlin("plugin.serialization")
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

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions.freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
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
        kotlinCompilerExtensionVersion = Versions.composeCompiler
    }

    packagingOptions {
        resources.excludes.add("/META-INF/{AL2.0,LGPL2.1}")
    }

    // Possible Compose Destinations configs:
//    ksp {
//        // Module name.
//        // It will be used as the generated sealed Destinations prefix
//        arg("compose-destinations.moduleName", "featureX")
//
//        // Can be:
//        // - "destinations":
//        // Generates the destinations and exposes a list with all of them. Doesn't generate any nav graphs
//        //
//        // - "navgraphs":
//        // Generates destinations and nav graph(s) individually without nesting any of them
//        //
//        // - "singlemodule" (default):
//        // Generates destinations and nav graphs nesting all graphs inside the default "root" one.
//        // Also creates a CoreExtensions.kt file with useful utilities for a single module case.
//        arg("compose-destinations.mode", "destinations")
//        // If you have a single module but you want to manually create the nav graphs, use this:
//        arg("compose-destinations.generateNavGraphs", "false")
//
//        // To change the package name where the generated files will be placed
//        arg("compose-destinations.codeGenPackageName", "your.preferred.packagename")
//    }
}

dependencies {

    implementation(project(mapOf("path" to ":compose-destinations-animations")))
    implementation(project(mapOf("path" to ":composedestinations-ktx-serialization")))
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
