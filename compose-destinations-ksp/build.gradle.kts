plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(mapOf("path" to ":compose-destinations-codegen")))

    implementation(Deps.Ksp.api)
    implementation(Deps.Test.junit)
}