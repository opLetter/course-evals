#!/usr/bin/env kotlin
@file:Repository("https://repo1.maven.org/maven2/")
@file:DependsOn("io.github.typesafegithub:github-workflows-kt:2.3.0")

@file:Repository("https://bindings.krzeminski.it/")
@file:DependsOn("actions:checkout:v4")
@file:DependsOn("actions:setup-java:v4")
@file:DependsOn("gradle:actions__setup-gradle:v4")

import io.github.typesafegithub.workflows.actions.actions.Checkout
import io.github.typesafegithub.workflows.actions.actions.SetupJava
import io.github.typesafegithub.workflows.actions.gradle.ActionsSetupGradle
import io.github.typesafegithub.workflows.dsl.JobBuilder
import io.github.typesafegithub.workflows.dsl.expressions.Contexts
import io.github.typesafegithub.workflows.dsl.expressions.expr

@Suppress("PropertyName")
val EVALS_DATA_TOKEN by Contexts.secrets

fun JobBuilder<*>.setUpWithData() {
    uses(name = "Checkout code", action = Checkout())

    uses(
        name = "Checkout data",
        action = Checkout(
            repository = "opletter/course-evals-data",
            path = "data",
            token = expr { EVALS_DATA_TOKEN }
        )
    )

    uses(
        name = "Set up Java",
        action = SetupJava(javaVersion = "17", distribution = SetupJava.Distribution.Temurin)
    )

    uses(
        name = "Setup Gradle",
        action = ActionsSetupGradle()
    )
}