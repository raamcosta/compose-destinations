plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation(libs.gradle.mavenPublish)
}

gradlePlugin {
    plugins {
        register("PublishConventionPlugin") {
            id = "composedestinations.convention.publish"
            implementationClass = "PublishConventionPlugin"
        }
    }
}