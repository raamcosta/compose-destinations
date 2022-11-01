plugins {
    kotlin("jvm")
}

apply(from = "${rootProject.projectDir}/publish.gradle")

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions.freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
}

dependencies {
    implementation(project(mapOf("path" to ":compose-destinations-codegen")))

    implementation(libs.ksp.api)
    implementation(libs.test.junit)
}
