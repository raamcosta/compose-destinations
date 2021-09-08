plugins {
    kotlin("jvm")
    id("maven-publish")
}

dependencies {
    implementation(project(mapOf("path" to ":compose-destinations-codegen")))

    implementation(Deps.Ksp.api)
    implementation(Deps.Test.junit)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = "com.github.raamcosta.compose-destinations"
            artifactId = "ksp"

            from(components["java"])
        }
    }
}
