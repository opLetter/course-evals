@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.coroutines)
    implementation(libs.serialization)
    implementation(libs.ktor.client.cio)
    implementation(libs.bundles.ktor.main)
    implementation(libs.pdfbox)
    implementation(project(":common"))
}

kotlin {
    jvmToolchain(11)
}

application {
    mainClass.set("io.github.opletter.courseevals.fsu.MainKt")
}