import java.net.URI

include(":core")


include(":auth")





pluginManagement {
    repositories {
        mavenCentral()
        google()
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

rootProject.name = "you4me"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
include(":app")

 