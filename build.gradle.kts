// Top-level build file where you can add configuration options common to all sub-projects/modules.
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    id(Deps.Gradle.dependencyCheckPlugin) version Versions.dependencyCheckPlugin
}

buildscript {

    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        with(Deps.Gradle) {
            classpath(pluginVersion)
            classpath(kotlin)
            classpath("com.vanniktech:gradle-maven-publish-plugin:0.18.0")
        }
    }
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}

/**
 * Run ./gradlew dependencyUpdates to check for new updates
 * in dependencies used.
 * More info at: https://github.com/ben-manes/gradle-versions-plugin
 */
tasks.withType<DependencyUpdatesTask> {
    rejectVersionIf {
        // Don't allow non stable versions, unless we are already using one for this dependency
        isNonStable(candidate.version) && !isNonStable(currentVersion)
    }
}

/**
 * Decides if this version is stable or not.
 */
fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.toUpperCase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return !isStable
}