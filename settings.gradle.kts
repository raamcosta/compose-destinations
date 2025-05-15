dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "ComposeDestinations"

include(":compose-destinations")
include(":compose-destinations-ksp")
include(":compose-destinations-codegen")
include(":compose-destinations-bottom-sheet")
include(":compose-destinations-wear")
include(":sample")
include(":sample-wear")
include(":sample-wear-m3")
include(":playground:app")
include(":playground:core")
include(":playground:featurex")
include(":playground:featurey")
include(":playground:featurey:sub")
include(":playground:featurez")
