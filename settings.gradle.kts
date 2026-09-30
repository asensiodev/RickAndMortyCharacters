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

rootProject.name = "RickAndMortyCharacters"

include(":app")
include(":feature:home")
include(":feature:details")
include(":domain:characters")
include(":data:characters")
include(":core:designsystem")
