pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://us-central1-maven.pkg.dev/varabyte-repos/public")
    }
}

rootProject.name = "course-evals"

include(":common")
include(":site-core")
include(":site")
include(":fsu")
include(":ttu")
include(":rutgers:extension")
include(":rutgers:core")
include(":rutgers:extension-test")