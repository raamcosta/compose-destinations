// Top-level build file where you can add configuration options common to all subprojects/modules.
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

// TODO: Remove this after https://youtrack.jetbrains.com/issue/KTIJ-19369 is resolved.
@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.dependencyCheckPlugin)
}

buildscript {

    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath(libs.pluginVersion)
        classpath(libs.kotlin)
        classpath(libs.kotlinSerialization)
        classpath(libs.mavenPublishPlugin)
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
        // Don't allow non-stable versions, unless we are already using one for this dependency
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
