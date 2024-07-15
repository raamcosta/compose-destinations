import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.compose.compiler)
    id("com.google.devtools.ksp") version libs.versions.ksp.get()
    kotlin("plugin.parcelize")
    kotlin("plugin.serialization")
}

// Possible Compose Destinations configs:
ksp {
//        // Module name.
//        // It will be used as the generated sealed Destinations prefix
//        arg("compose-destinations.moduleName", "featureX")

//        // If you want to manually create the nav graphs, use this:
//        arg("compose-destinations.generateNavGraphs", "false")
    arg("compose-destinations.htmlMermaidGraph", "$rootDir/samples/playground/docs")
    arg("compose-destinations.mermaidGraph", "$rootDir/samples/playground/docs")

    // To change the package name where the generated files will be placed
    arg("compose-destinations.codeGenPackageName", "com.ramcosta.samples.playground.ui.screens")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-opt-in=kotlin.RequiresOptIn"
        )
    }
    applyDefaultHierarchyTemplate()
    jvmToolchain(11)
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    jvm("desktop")

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }

        val commonMain by getting {
            kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")

            dependencies {
                implementation(project(":core"))
                implementation(project(":samples:playground:core"))
                implementation(project(":samples:playground:featurex"))
                implementation(project(":samples:playground:featurey"))
                implementation(project(":samples:playground:featurez"))

                implementation(libs.toaster)
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
            implementation(project(":bottom-sheet"))

            implementation(compose.preview)
            implementation(libs.androidx.activityCompose)
        }
    }

    // following example here:
    // https://github.com/OliverO2/kotlin-multiplatform-ksp/blob/main/base/build.gradle.kts
    dependencies {
        // Provide symbol processing for each Kotlin '*Main' source set.
        kotlin.sourceSets.forEach { sourceSet ->

            if (sourceSet.name == "appleMain"
                || sourceSet.name == "iosMain"
                || sourceSet.name == "nativeMain"
                || !sourceSet.name.endsWith("Main")) {
                println("RACOSTA ignoring set ${sourceSet.name}")
                return@forEach
            }

            println("RACOSTA src set -> ${sourceSet.name}")

            val kspConfiguration = when {
                sourceSet.name == "commonMain" -> "kspCommonMainMetadata"
                // but skip configurations for each platform-specific source set
                sourceSet.name.endsWith("Main") -> "ksp${sourceSet.name.substringBefore("Main").replaceFirstChar { it.titlecase() }}"
                else -> null
            }
            if (kspConfiguration != null) add(kspConfiguration, project(":ksp"))
        }
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
    namespace = "com.ramcosta.samples.playground"
    compileSdk = libs.versions.compileSdk.get().toIntOrNull()

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        applicationId = "com.ramcosta.samples.playground"
        minSdk = libs.versions.minSdk.get().toIntOrNull()
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

    buildFeatures {
        compose = true
    }

    packaging {
        resources.excludes.add("/META-INF/{AL2.0,LGPL2.1}")
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.ramcosta.samples.playground"
            packageVersion = "1.0.0"
        }
    }
}

//dependencies {
//
////    implementation(project(":core"))
////    implementation(project(":bottom-sheet"))
////    implementation(project(":samples:playground:core"))
////    implementation(project(":samples:playground:featurex"))
////    implementation(project(":samples:playground:featurey"))
////    implementation(project(":samples:playground:featurez"))
////    "ksp"(project(":ksp"))
//
////    debugImplementation("androidx.compose.ui:ui-tooling:1.6.2")
////    implementation("androidx.compose.ui:ui-tooling-preview:1.6.2")
//
//    implementation(libs.androidMaterial)
//
//    implementation(libs.compose.ui)
//    implementation(libs.compose.material)
//    implementation(libs.compose.viewModel)
//
//    implementation(libs.androidx.lifecycleRuntimeKtx)
////    implementation(libs.androidx.activityCompose)
//
////    implementation(libs.ktxSerializationJson)
//
//    testImplementation(libs.test.junit)
//    testImplementation(libs.test.mockk)
//    testImplementation(project(":ksp"))
//
//    testImplementation(libs.test.kotlinCompile)
//    testImplementation(libs.test.kotlinCompileKsp)
//}
