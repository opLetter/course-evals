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
    implementation(projects.common)
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass = "io.github.opletter.courseevals.usf.MainKt"
}