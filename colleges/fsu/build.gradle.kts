plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    application
}

dependencies {
    implementation(libs.coroutines)
    implementation(libs.serialization)
    implementation(libs.ktor.client.cio)
    implementation(libs.bundles.ktor.main)
    implementation(libs.pdfbox)
    implementation(projects.common)
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass = "io.github.opletter.courseevals.fsu.MainKt"
}