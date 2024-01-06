plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    application
}

repositories {
    mavenCentral()
}

kotlin {
    jvm {
        withJava()
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

application {
    mainClass = "io.github.opletter.courseevals.rutgers.MainKt"
}