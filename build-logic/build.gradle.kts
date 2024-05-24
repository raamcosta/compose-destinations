plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
}

//java {
//    toolchain.languageVersion.set(JavaLanguageVersion.of(JavaVersion.VERSION_17.toString()))
//}
//
//tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
//    kotlinOptions {
//        jvmTarget = JavaVersion.VERSION_17.toString()
//    }
//}

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