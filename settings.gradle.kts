pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

plugins {
    id("jp.co.gahojin.refreshVersions") version "0.1.3"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

refreshVersions {
    sortSection = true
}

rootProject.name = "location-coroutines"
include("location-coroutines")
include(":sample")
