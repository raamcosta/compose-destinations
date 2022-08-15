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

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions.freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
}

dependencies {
    implementation(project(mapOf("path" to ":compose-destinations-codegen")))

    implementation(libs.ksp.api)
    implementation(libs.test.junit)
}
