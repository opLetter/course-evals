pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://us-central1-maven.pkg.dev/varabyte-repos/public")
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "course-evals"

include(":common")
include(":site-core")
include(":site")
include(":colleges:fsu")
include(":colleges:rutgers")
include(":colleges:txst")
include(":colleges:usf")