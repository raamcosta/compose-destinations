// Top-level build file where you can add configuration options common to all subprojects/modules.
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    alias(libs.plugins.dependencyCheckPlugin)
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.androidApplication) apply false

    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinAndroid) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.compose.compiler) apply false

    alias(libs.plugins.mavenPublish) apply false
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
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
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return !isStable
}
