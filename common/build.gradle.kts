plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
}

group = "io.github.opletter.courseevals.common"
version = "1.0-SNAPSHOT"

kotlin {
    jvm()
    js {
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.serialization)
            implementation(libs.coroutines)
            implementation(libs.bundles.ktor.main)
        }

        jvmMain.dependencies {
            implementation(libs.ktor.client.cio)
        }
    }
}