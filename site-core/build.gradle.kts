import com.varabyte.kobweb.gradle.library.util.configAsKobwebLibrary

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
        jsMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.html.core)
            implementation(libs.bundles.kobweb)
            implementation(projects.common)
        }
    }
}