#!/usr/bin/env kotlin
@file:DependsOn("io.github.typesafegithub:github-workflows-kt:1.11.0")

import io.github.typesafegithub.workflows.actions.actions.CheckoutV4
import io.github.typesafegithub.workflows.actions.actions.SetupJavaV4
import io.github.typesafegithub.workflows.actions.gradle.GradleBuildActionV3
import io.github.typesafegithub.workflows.dsl.JobBuilder
import io.github.typesafegithub.workflows.dsl.expressions.Contexts
import io.github.typesafegithub.workflows.dsl.expressions.expr

@Suppress("PropertyName")
val EVALS_DATA_TOKEN by Contexts.secrets

fun JobBuilder<*>.setUpWithData() {
    uses(name = "Checkout code", action = CheckoutV4())

    uses(
        name = "Checkout data",
        action = CheckoutV4(
            repository = "opletter/course-evals-data",
            path = "data",
            token = expr { EVALS_DATA_TOKEN }
        )
    )

    uses(
        name = "Set up Java",
        action = SetupJavaV4(javaVersion = "17", distribution = SetupJavaV4.Distribution.Temurin)
    )

    uses(
        name = "Setup Gradle",
        action = GradleBuildActionV3(
            buildScanPublish = true,
            buildScanTermsOfServiceUrl = "https://gradle.com/terms-of-service",
            buildScanTermsOfServiceAgree = true,
        )
    )
}