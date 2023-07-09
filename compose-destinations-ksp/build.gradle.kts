plugins {
    kotlin("jvm")
}

apply(from = "${rootProject.projectDir}/publish.gradle")

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions.freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }
}

dependencies {
    implementation(project(mapOf("path" to ":compose-destinations-codegen")))

    implementation(libs.ksp.api)
    implementation(libs.test.junit)
}
