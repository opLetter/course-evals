import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    jvm {
        // This creates a task called "runJvm", which we can just call as "run" due to how Gradle works, matching the
        // behavior of JVM-only projects.
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        binaries {
            executable {
                mainClass = "io.github.opletter.courseevals.rutgers.MainKt"
            }
        }
    }
    js {
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.coroutines)
            implementation(libs.serialization)
            implementation(libs.bundles.ktor.main)
            implementation(projects.common)
        }

        jvmMain.dependencies {
            implementation(libs.ktor.client.cio)
        }
    }
}