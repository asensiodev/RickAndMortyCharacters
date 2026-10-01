pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "RickAndMortyCharacters"

include(":app")
include(":feature:home")
include(":feature:details")
include(":domain:characters")
include(":data:characters")
include(":core:designsystem")
include(":core:testing")
