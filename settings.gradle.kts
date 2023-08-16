pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://us-central1-maven.pkg.dev/varabyte-repos/public")
    }
}

plugins {
    id("com.gradle.enterprise") version ("3.13.3")
}

gradleEnterprise {
    if (System.getenv("CI") != null) {
        buildScan {
            termsOfServiceUrl = "https://gradle.com/terms-of-service"
            termsOfServiceAgree = "yes"
        }
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