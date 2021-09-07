plugins {
    kotlin("jvm")
    id("maven-publish")
}

dependencies {
    implementation(project(mapOf("path" to ":compose-destinations-codegen")))

    implementation(Deps.Ksp.api)
    implementation(Deps.Test.junit)
}

tasks.jar {
    from(project(":compose-destinations-codegen").sourceSets.main.get().output)
}

apply {
    from("publish.gradle")
}
