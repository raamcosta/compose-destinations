plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.compose.compiler)
}

apply(from = "${rootProject.projectDir}/publish.gradle")

kotlin {
//    applyDefaultHierarchyTemplate()
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = JavaVersion.VERSION_1_8.toString()
            }
        }
    }
    jvm()
    macosX64()
    macosArm64()
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
//        val androidMain by getting
//        val jvmMain by getting

        commonMain.dependencies {
            api("org.jetbrains.androidx.navigation:navigation-compose:2.8.0-alpha02")
        }

        androidMain.dependencies {
//            api(libs.compose.navigation)
            api("org.jetbrains.androidx.core:core-bundle:1.0.0") // TODO RACOSTA why do I need this?
        }

//        androidMain.dependsOn(jvmMain)
    }

    compilerOptions {
        freeCompilerArgs.addAll(
            "-opt-in=kotlin.RequiresOptIn",
            "-opt-in=com.ramcosta.composedestinations.annotation.internal.InternalDestinationsApi"
        )
    }
}

android {

    namespace = "com.ramcosta.composedestinations"
    compileSdk = libs.versions.compileSdk.get().toIntOrNull()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toIntOrNull()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles.add(File("consumer-rules.pro"))
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    lint {
        disable.add("EmptyNavDeepLink")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildFeatures {
        compose = true
    }
}
