pluginManagement {
    includeBuild("build-logic")
    repositories {
        gradlePluginPortal()
        google()
    }
}

include(":app", ":ass")
