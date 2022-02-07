plugins {
    kotlin("jvm")
    id("maven-publish")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions.freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
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
