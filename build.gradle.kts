buildscript {

    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.0.1")
        classpath(Deps.Gradle.kotlin)
    }
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}