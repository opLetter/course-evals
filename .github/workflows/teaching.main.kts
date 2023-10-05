#!/usr/bin/env kotlin
@file:DependsOn("io.github.typesafegithub:github-workflows-kt:1.2.0")

import io.github.typesafegithub.workflows.actions.actions.CheckoutV4
import io.github.typesafegithub.workflows.actions.actions.SetupJavaV3
import io.github.typesafegithub.workflows.domain.RunnerType.UbuntuLatest
import io.github.typesafegithub.workflows.domain.triggers.Cron
import io.github.typesafegithub.workflows.domain.triggers.Schedule
import io.github.typesafegithub.workflows.domain.triggers.WorkflowDispatch
import io.github.typesafegithub.workflows.dsl.expressions.Contexts
import io.github.typesafegithub.workflows.dsl.expressions.expr
import io.github.typesafegithub.workflows.dsl.workflow
import io.github.typesafegithub.workflows.yaml.writeToFile

val EVALS_DATA_TOKEN by Contexts.secrets

fun teachingDataWorkflow(name: String, college: String, gradleCommand: String, cron: Cron) = workflow(
    name = name,
    on = listOf(
        WorkflowDispatch(),
        Schedule(listOf(cron))
    ),
    sourceFile = __FILE__.toPath(),
    targetFileName = "teaching_data_${college.lowercase()}.yml"
) {
    job(id = "get_and_commit", runsOn = UbuntuLatest) {
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
            action = SetupJavaV3(javaVersion = "17", distribution = SetupJavaV3.Distribution.Temurin)
        )
        run(name = "Run", command = "$gradleCommand --args=\"-teaching\"")
        run(
            name = "Add & Commit",
            command = """
              cd data
              git config user.name "github-actions[bot]"
              git config user.email "41898282+github-actions[bot]@users.noreply.github.com"
              git checkout master
              git add .
              if ! git diff-index --quiet HEAD; then
                git commit -m "$college: Updated teaching data"
                git push
              else
                echo "CHANGE=false" >> ${'$'}GITHUB_OUTPUT
              fi
            """.trimIndent()
        )
    }
}

teachingDataWorkflow(
    name = "FSU: Update Teaching Data",
    college = "FSU",
    gradleCommand = "./gradlew colleges:fsu:run",
    cron = Cron(minute = "0", hour = "16", dayWeek = "1")
).writeToFile(addConsistencyCheck = false)

teachingDataWorkflow(
    name = "USF: Update Teaching Data",
    college = "USF",
    gradleCommand = "./gradlew colleges:usf:run",
    cron = Cron(minute = "0", hour = "22", dayWeek = "1-5")
).writeToFile(addConsistencyCheck = false)