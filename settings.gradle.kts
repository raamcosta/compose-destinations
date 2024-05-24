pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "ComposeDestinations"

include(":core")
include(":ksp")
include(":codegen")
include(":bottom-sheet")
include(":wear-core")
include(":sample")
include(":sample-wear")
include(":playground:app")
include(":playground:core")
include(":playground:featurex")
include(":playground:featurey")
include(":playground:featurey:sub")
include(":playground:featurez")
