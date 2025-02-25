import org.jetbrains.kotlin.gradle.InternalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.targets.jvm.tasks.KotlinJvmRun

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    jvm()
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


// Hack: Recreate the `jvmRun` task as `run`
// The `jvmRun` task works great, but I want a task named `run` for consistency with non-multiplatform projects
@OptIn(InternalKotlinGradlePluginApi::class)
tasks.register<KotlinJvmRun>("run") {
    mainClass = "io.github.opletter.courseevals.rutgers.MainKt"
    val mainCompilation = kotlin.jvm().compilations.getByName("main")
    classpath(mainCompilation.output.allOutputs, mainCompilation.runtimeDependencyFiles)
}