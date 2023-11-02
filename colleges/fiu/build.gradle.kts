plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    application
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation(libs.coroutines)
    implementation(libs.serialization)
    implementation(libs.ktor.client.cio)
    implementation(libs.bundles.ktor.main)
    // not published anywhere, need to use mavenLocal (https://github.com/opLetter/tableau-scraper-kt)
    implementation("io.github.opletter.tableau:tableau-scraper-kt:0.0.1-SNAPSHOT")
    implementation(projects.common)
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass = "io.github.opletter.courseevals.fiu.MainKt"
}