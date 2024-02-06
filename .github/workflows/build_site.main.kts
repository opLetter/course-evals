#!/usr/bin/env kotlin
@file:DependsOn("io.github.typesafegithub:github-workflows-kt:1.11.0")
@file:Import("custom_actions.main.kts")

import io.github.typesafegithub.workflows.actions.actions.CacheV4
import io.github.typesafegithub.workflows.actions.actions.CheckoutV4
import io.github.typesafegithub.workflows.actions.actions.SetupJavaV4
import io.github.typesafegithub.workflows.actions.gradle.GradleBuildActionV3
import io.github.typesafegithub.workflows.domain.Concurrency
import io.github.typesafegithub.workflows.domain.Mode
import io.github.typesafegithub.workflows.domain.Permission
import io.github.typesafegithub.workflows.domain.RunnerType.UbuntuLatest
import io.github.typesafegithub.workflows.domain.triggers.*
import io.github.typesafegithub.workflows.dsl.expressions.expr
import io.github.typesafegithub.workflows.dsl.workflow
import io.github.typesafegithub.workflows.yaml.writeToFile

val KOBWEB_CLI_VERSION = "0.9.13"

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
    sourceFile = __FILE__.toPath(),
    targetFileName = "build_and_deploy_site.yml"
) {
    val exportJob = job(id = "export", runsOn = UbuntuLatest) {
        uses(name = "Checkout", action = CheckoutV4())

        uses(
            name = "Set up Java",
            action = SetupJavaV4(javaVersion = "17", distribution = SetupJavaV4.Distribution.Temurin)
        )

        uses(
            name = "Setup Gradle",
            action = GradleBuildActionV3(
                buildScanTermsOfServiceUrl = "https://gradle.com/terms-of-service",
                buildScanTermsOfServiceAgree = true,
            )
        )

        val browserCacheStep = run(
            name = "Query Browser Cache ID",
            command = "echo \"value=$(./gradlew -q :site:kobwebBrowserCacheId)\" >> \$GITHUB_OUTPUT"
        )

        uses(
            name = "Cache Browser Dependencies",
            action = CacheV4(
                path = listOf("~/.cache/ms-playwright"),
                key = "${expr { runner.os }}-playwright-${expr { "steps.${browserCacheStep.id}.outputs.value" }}"
            )
        )

        uses(
            name = "Fetch kobweb",
            action = ReleaseDownloaderV1(
                repository = "varabyte/kobweb-cli",
                tag = "v$KOBWEB_CLI_VERSION",
                fileName = "kobweb-$KOBWEB_CLI_VERSION.tar",
                tarBall = false,
                zipBall = false,
                extract = true
            )
        )

        run(
            name = "Run export",
            command = """
              cd site
              ../kobweb-$KOBWEB_CLI_VERSION/bin/kobweb export --notty --layout static --gradle-export "--scan"
            """.trimIndent()
        )

        uses(
            name = "Upload artifact",
            action = UploadPagesArtifactV3(path = "./site/.kobweb/site")
        )
    }
    val deploymentId = "deployment"
    job(
        id = "deploy",
        runsOn = UbuntuLatest,
        needs = listOf(exportJob),
        _customArguments = mapOf(
            "environment" to mapOf(
                "name" to "github-pages",
                "url" to expr { "steps.$deploymentId.outputs.page_url" }
            )
        )
    ) {
        uses(
            action = DeployPagesV4(),
            _customArguments = mapOf("id" to deploymentId)
        )
    }
}.writeToFile(addConsistencyCheck = false)