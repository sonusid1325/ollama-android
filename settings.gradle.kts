pluginManagement {
    repositories {
        // Google hosts AGP/AndroidX, Maven Central covers most libraries, and
        // Gradle Plugin Portal provides the Kotlin Android plugin (2.0.21),
        // so the combination guarantees plugin resolution without third-party
        // repositories or group filtering.
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
        maven(url = "https://jitpack.io")
    }
}

rootProject.name = "Ollama"
include(":app")
