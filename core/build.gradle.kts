import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinHierarchyBuilder

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    id("composedestinations.convention.publish")
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_1_8)
        }
    }
    jvm()
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    applyDefaultHierarchyTemplate {
        jvmAndAndroid()
    }

    sourceSets {

        commonMain.dependencies {
            api(libs.kmp.compose.navigation)
        }

        androidMain.dependencies {
            api(libs.kmp.bundle) // TODO RACOSTA why do I need this?
        }
    }

    @Suppress("OPT_IN_USAGE")
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

@OptIn(ExperimentalKotlinGradlePluginApi::class)
fun KotlinHierarchyBuilder.Root.jvmAndAndroid() {
    common {
        group("jvmAndAndroid") {
            withJvm()
            withAndroidTarget()
        }
    }
}