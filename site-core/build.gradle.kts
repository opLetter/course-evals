import com.varabyte.kobweb.gradle.library.util.configAsKobwebLibrary
import org.jetbrains.kotlin.gradle.targets.js.dukat.DukatTask

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.kobweb.library)
    alias(libs.plugins.kotlin.serialization)
}

group = "io.github.opletter.courseevals.site.core"
version = "1.0-SNAPSHOT"

kotlin {
    configAsKobwebLibrary()

    sourceSets {
        @Suppress("UNUSED_VARIABLE")
        val jsMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.web.core)
                implementation(libs.bundles.kobweb)
                implementation(project(":common"))
            }
        }
    }
}

tasks.withType<DukatTask> { enabled = false } // disable unnecessary task for minor performance boost