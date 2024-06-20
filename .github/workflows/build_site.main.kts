#!/usr/bin/env kotlin
@file:Repository("https://repo1.maven.org/maven2/")
@file:DependsOn("io.github.typesafegithub:github-workflows-kt:2.0.0")

@file:Repository("https://bindings.krzeminski.it/")
@file:DependsOn("actions:checkout:v4")
@file:DependsOn("actions:setup-java:v4")
@file:DependsOn("actions:cache:v4")
@file:DependsOn("gradle:actions__setup-gradle:v3")
@file:DependsOn("actions:upload-pages-artifact:v3")
@file:DependsOn("actions:deploy-pages:v4")
@file:DependsOn("robinraju:release-downloader:v1.10")

import io.github.typesafegithub.workflows.actions.actions.*
import io.github.typesafegithub.workflows.actions.gradle.ActionsSetupGradle
import io.github.typesafegithub.workflows.actions.robinraju.ReleaseDownloader
import io.github.typesafegithub.workflows.domain.Concurrency
import io.github.typesafegithub.workflows.domain.Environment
import io.github.typesafegithub.workflows.domain.Mode
import io.github.typesafegithub.workflows.domain.Permission
import io.github.typesafegithub.workflows.domain.RunnerType.UbuntuLatest
import io.github.typesafegithub.workflows.domain.triggers.Push
import io.github.typesafegithub.workflows.domain.triggers.WorkflowDispatch
import io.github.typesafegithub.workflows.dsl.expressions.expr
import io.github.typesafegithub.workflows.dsl.workflow
import io.github.typesafegithub.workflows.yaml.ConsistencyCheckJobConfig

val KOBWEB_CLI_VERSION = "0.9.15"

workflow(
    name = "Deploy Kobweb site to Pages",
    on = listOf(
        WorkflowDispatch(),
        Push(
            branches = listOf("master"),
            paths = listOf("site/**", "site-core/**", "common/**", "gradle/**", "*.gradle.kts")
        )
    ),
    permissions = mapOf(
        Permission.Contents to Mode.Read,
        Permission.Pages to Mode.Write,
        Permission.IdToken to Mode.Write
    ),
    concurrency = Concurrency(group = "pages", cancelInProgress = true),
    sourceFile = __FILE__,
    targetFileName = "build_and_deploy_site.yml",
    consistencyCheckJobConfig = ConsistencyCheckJobConfig.Disabled,
) {
    val exportJob = job(id = "export", runsOn = UbuntuLatest) {
        uses(name = "Checkout", action = Checkout())

        uses(
            name = "Set up Java",
            action = SetupJava(javaVersion = "17", distribution = SetupJava.Distribution.Temurin)
        )

        uses(
            name = "Setup Gradle",
            action = ActionsSetupGradle(validateWrappers = true)
        )

        val browserCacheStep = run(
            name = "Query Browser Cache ID",
            command = "echo \"value=$(./gradlew -q :site:kobwebBrowserCacheId)\" >> \$GITHUB_OUTPUT"
        )

        uses(
            name = "Cache Browser Dependencies",
            action = Cache(
                path = listOf("~/.cache/ms-playwright"),
                key = "${expr { runner.os }}-playwright-${expr { "steps.${browserCacheStep.id}.outputs.value" }}"
            )
        )

        uses(
            name = "Fetch kobweb",
            action = ReleaseDownloader(
                repository = "varabyte/kobweb-cli",
                tag = "v$KOBWEB_CLI_VERSION",
                fileName = "kobweb-$KOBWEB_CLI_VERSION.tar",
                // these are in theory booleans
                tarBall = "false",
                zipBall = "false",
                extract = "true",
            )
        )

        run(
            name = "Run export",
            command = """
              kobweb-$KOBWEB_CLI_VERSION/bin/kobweb export -p site --notty --layout static --gradle-export "--scan"
            """.trimIndent()
        )

        uses(
            name = "Upload artifact",
            action = UploadPagesArtifact(path = "./site/.kobweb/site")
        )
    }
    val deploymentId = "deployment"
    job(
        id = "deploy",
        runsOn = UbuntuLatest,
        needs = listOf(exportJob),
        environment = Environment(
            name = "github-pages",
            url = expr { "steps.$deploymentId.outputs.page_url" }
        ),
    ) {
        uses(
            action = DeployPages(),
            _customArguments = mapOf("id" to deploymentId)
        )
    }
}