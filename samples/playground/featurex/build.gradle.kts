import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    id("com.google.devtools.ksp") version libs.versions.ksp.get()
    kotlin("plugin.serialization")
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

    jvmToolchain(8)
    applyDefaultHierarchyTemplate()

    sourceSets {
        val commonMain by getting {
            kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")

            dependencies {
                implementation(project(":core"))

                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)
                implementation(libs.ktxSerializationJson)
            }
        }

        androidMain.dependencies {
        }
    }

    ksp {
        arg("compose-destinations.moduleName", "featureX")
        arg("compose-destinations.htmlMermaidGraph", "$rootDir/playground/docs")
        arg("compose-destinations.mermaidGraph", "$rootDir/playground/docs")
    }

    @Suppress("OPT_IN_USAGE")
    compilerOptions {
        freeCompilerArgs.addAll(
            "-opt-in=kotlin.RequiresOptIn",
        )
    }

    dependencies {
        add("kspCommonMainMetadata", project(":ksp"))
    }
}

gradle.projectsEvaluated {
    tasks {
        val kspCommonMainKotlinMetadata by getting
        withType<KotlinCompilationTask<*>> {
            if (this !== kspCommonMainKotlinMetadata) {
                dependsOn(kspCommonMainKotlinMetadata)
            }
        }
    }
}

android {

    namespace = "com.ramcosta.playground.featurex"
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

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildFeatures {
        compose = true
    }
}

//dependencies {
//
//    implementation(project(":core"))
//    "ksp"(project(":ksp"))
//
//    implementation(libs.androidMaterial)
//
//    implementation(libs.compose.ui)
//    implementation(libs.compose.material)
//    implementation(libs.compose.viewModel)
//
//    implementation(libs.androidx.lifecycleRuntimeKtx)
//    implementation(libs.androidx.activityCompose)
//
//    implementation(libs.ktxSerializationJson)
//
//    testImplementation(libs.test.junit)
//    testImplementation(libs.test.mockk)
//    testImplementation(project(":ksp"))
//
//    testImplementation(libs.test.kotlinCompile)
//    testImplementation(libs.test.kotlinCompileKsp)
//}
