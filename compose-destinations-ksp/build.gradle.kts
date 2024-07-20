plugins {
    kotlin("jvm")
}

apply(from = "${rootProject.projectDir}/publish.gradle")

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-opt-in=kotlin.RequiresOptIn")
    }

    jvmToolchain(8)
}

dependencies {
    implementation(project(":compose-destinations-codegen"))

    implementation(libs.pprint)
    implementation(libs.ksp.api)
    implementation(libs.test.junit)
}
