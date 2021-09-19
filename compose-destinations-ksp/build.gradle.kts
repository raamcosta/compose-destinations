plugins {
    kotlin("jvm")
    id("com.vanniktech.maven.publish")
}

java {
    plugins.withId("com.vanniktech.maven.publish") {
        mavenPublish {
            sonatypeHost = com.vanniktech.maven.publish.SonatypeHost.S01
        }
    }
}

dependencies {
    implementation(project(mapOf("path" to ":compose-destinations-codegen")))

    implementation(Deps.Ksp.api)
    implementation(Deps.Test.junit)
}