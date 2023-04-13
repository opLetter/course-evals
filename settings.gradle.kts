pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://us-central1-maven.pkg.dev/varabyte-repos/public")
    }
}

plugins {
    id("com.gradle.enterprise") version ("3.9")
}

gradleEnterprise {
    if (System.getenv("CI") != null) {
        buildScan {
            publishAlways()
            termsOfServiceUrl = "https://gradle.com/terms-of-service"
            termsOfServiceAgree = "yes"
        }
    }
}

rootProject.name = "course-evals"

include(":common")
include(":site-core")
include(":site")
include(":rutgers:extension")
include(":rutgers:core")
include(":rutgers:extension-test")
include(":colleges:fsu")
include(":colleges:usf")