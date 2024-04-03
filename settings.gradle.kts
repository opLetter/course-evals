pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://us-central1-maven.pkg.dev/varabyte-repos/public")
    }
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://us-central1-maven.pkg.dev/varabyte-repos/public")
        mavenLocal {
            content {
                // https://github.com/opLetter/tableau-scraper-kt
                includeModule("io.github.opletter.tableau", "tableau-scraper-kt")
            }
        }
    }
}

plugins {
    id("com.gradle.develocity") version "3.17"
}

develocity {
    buildScan {
        publishing.onlyIf { false }
        if (System.getenv("CI") != null) {
            termsOfUseUrl = "https://gradle.com/terms-of-service"
            termsOfUseAgree = "yes"
        }
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "course-evals"

include(":common")
include(":site-core")
include(":site")
include(":colleges:fiu")
include(":colleges:fsu")
include(":colleges:rutgers")
include(":colleges:txst")
include(":colleges:usf")