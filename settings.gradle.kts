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

rootProject.name = "compose-destinations"

include(":core")
include(":ksp")
include(":codegen")
include(":bottom-sheet")
include(":wear-core")
include(":samples:sample")
include(":samples:sample-wear")
include(":samples:playground:app")
include(":samples:playground:core")
include(":samples:playground:featurex")
include(":samples:playground:featurey")
include(":samples:playground:featurey:sub")
include(":samples:playground:featurez")
