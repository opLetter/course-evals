plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
}

group = "io.github.opletter.courseevals.common"
version = "1.0-SNAPSHOT"

kotlin {
    jvm()
    js(IR) {
        browser()
    }

    @Suppress("UNUSED_VARIABLE")
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.serialization)
                implementation(libs.coroutines)
                implementation(libs.bundles.ktor.main)
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(libs.ktor.client.cio)
            }
        }
    }
}