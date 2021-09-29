pluginManagement {
    plugins {
        id("com.google.devtools.ksp") version "1.5.30-1.0.0"
        kotlin("jvm") version "1.5.30"
    }
    repositories {
        gradlePluginPortal()
        google()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "DestinationsTodoSample"

include(":app")
include(":compose-destinations")
include(":compose-destinations-ksp")
include(":compose-destinations-codegen")
