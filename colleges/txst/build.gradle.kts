plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    application
}

dependencies {
    implementation(libs.serialization)
    implementation(libs.bundles.ktor.main)
    implementation(projects.common)
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass = "io.github.opletter.courseevals.txst.MainKt"
}