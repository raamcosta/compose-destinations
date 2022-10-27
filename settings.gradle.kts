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
include(":compose-destinations-animations")
include(":sample")
include(":playground")
include(":playground-shared")
