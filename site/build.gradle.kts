import com.varabyte.kobweb.gradle.application.util.configAsKobwebApplication
import kotlinx.html.link
import kotlinx.html.script

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.kobweb.application)
    alias(libs.plugins.kotlin.serialization)
}

group = "io.github.opletter.courseevals.site"
version = "1.0-SNAPSHOT"

kobweb {
    app {
        index {
            description.set("View course evaluation results in an easy-to-read format")
            head.add {
                script {
                    consumer.onTagContent(
                        """
                    |
                    |      // For gh-pages 404 redirects. Credit: https://github.com/rafgraph/spa-github-pages
                    |      (function(l) {
                    |        if (l.search[1] === '/' ) {
                    |          var decoded = l.search.slice(1).split('&').map(function(s) { 
                    |            return s.replace(/~and~/g, '&')
                    |          }).join('?');
                    |          window.history.replaceState(null, null,
                    |              l.pathname.slice(0, -1) + decoded + l.hash
                    |          );
                    |        }
                    |      }(window.location))
                    |    """.trimMargin()
                    )
                }
                comment("Text Balancing. Credit: https://github.com/adobe/balance-text")
                consumer.onTagContent("\n\t")
                script(src = "https://cdnjs.cloudflare.com/ajax/libs/balance-text/3.3.1/balancetext.min.js") {}
                consumer.onTagContent("\n\t")
                script(src = "https://gc.zgo.at/count.js") {
//                    async = true
//                    attributes["data-goatcounter-settings"] = "{\"allow_local\": true}"
                    attributes["data-goatcounter"] = "https://course-evals.goatcounter.com/count"
                }
                link(
                    href = "https://fonts.googleapis.com/css2?family=Montserrat:wght@800&text=EVALS&display=block",
                    rel = "stylesheet"
                )
            }
        }
    }
}

kotlin {
    configAsKobwebApplication("course-evals")

    sourceSets {
        @Suppress("UNUSED_VARIABLE")
        val jsMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.html.core)
                implementation(libs.bundles.kobweb)
                implementation(project(":common"))
                implementation(project(":site-core"))
            }
        }
    }
}

// decreases js bundle size (possible a ktor-only issue? see: https://youtrack.jetbrains.com/issue/KTOR-1084)
tasks.withType<org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile>().named("compileProductionExecutableKotlinJs") {
    kotlinOptions.freeCompilerArgs += "-Xir-per-module"
}

// for export, we want per-module compilation (as set above)
// however, this causes the dev script to also be per-module, which breaks it when export tries to run it
// so, we overwrite the dev script with the prod script - which has been compiled with webpack
val exportHackTask = tasks.register("exportHackTask") {
    val projectPath = projectDir.toPath()
    val folder = com.varabyte.kobweb.project.KobwebFolder.inPath(projectPath)!!
    val conf = com.varabyte.kobweb.project.conf.KobwebConfFile(folder).content!!

    val devScript = projectPath.resolve(File(conf.server.files.dev.script).toPath()).toFile()
    val prodScript = projectPath.resolve(File(conf.server.files.prod.script).toPath()).toFile()

    inputs.file(prodScript)
    outputs.file(devScript)

    doLast {
        prodScript.copyTo(devScript, overwrite = true)
    }
}

afterEvaluate {
    tasks.named("kobwebExport") {
        dependsOn(exportHackTask)
    }
    exportHackTask.configure {
        dependsOn(tasks.named("jsBrowserProductionWebpack"))
    }
}