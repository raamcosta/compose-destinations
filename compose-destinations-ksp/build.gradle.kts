plugins {
    kotlin("jvm")
}

apply(from = "${rootProject.projectDir}/publish.gradle")

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions.freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
}

dependencies {
    implementation(project(":compose-destinations-codegen"))

    implementation(libs.pprint)
    implementation(libs.ksp.api)
    implementation(libs.test.junit)
}
