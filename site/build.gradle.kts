import com.varabyte.kobweb.gradle.application.util.configAsKobwebApplication
import kotlinx.html.link
import kotlinx.html.script

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kobweb.application)
    alias(libs.plugins.kotlin.serialization)
}

group = "io.github.opletter.courseevals.site"
version = "1.0-SNAPSHOT"

kobweb {
    app {
        index {
            description = "View course evaluation results in an easy-to-read format"
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
    configAsKobwebApplication(rootProject.name)
    js().compilerOptions {
        target = "es2015"
    }

    sourceSets {
        jsMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.html.core)
            implementation(libs.bundles.kobweb)
            implementation(projects.common)
            implementation(projects.siteCore)
        }
    }
}