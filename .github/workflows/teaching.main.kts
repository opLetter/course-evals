#!/usr/bin/env kotlin
@file:Repository("https://repo1.maven.org/maven2/")
@file:DependsOn("io.github.typesafegithub:github-workflows-kt:3.2.0")

@file:Repository("https://bindings.krzeminski.it/")
@file:DependsOn("EndBug:add-and-commit:v9")

@file:Import("common_setup.main.kts")

import io.github.typesafegithub.workflows.actions.endbug.AddAndCommit
import io.github.typesafegithub.workflows.domain.RunnerType.UbuntuLatest
import io.github.typesafegithub.workflows.domain.triggers.Cron
import io.github.typesafegithub.workflows.domain.triggers.Schedule
import io.github.typesafegithub.workflows.domain.triggers.WorkflowDispatch
import io.github.typesafegithub.workflows.dsl.workflow
import io.github.typesafegithub.workflows.yaml.ConsistencyCheckJobConfig

fun teachingDataWorkflow(college: String, cron: Cron, gradleCommand: String = getGradleCommand(college)) = workflow(
    name = "$college: Update Teaching Data",
    on = listOf(
        WorkflowDispatch(),
        Schedule(listOf(cron))
    ),
    sourceFile = __FILE__,
    targetFileName = "teaching_data_${college.lowercase()}.yml",
    consistencyCheckJobConfig = ConsistencyCheckJobConfig.Disabled,
) {
    job(id = "get_and_commit", runsOn = UbuntuLatest) {
        setUpWithData()

        run(name = "Run", command = gradleCommand)

        uses(
            name = "Add & Commit",
            action = AddAndCommit(
                add = "-A",
                cwd = "data",
                defaultAuthor = AddAndCommit.DefaultAuthor.GithubActions,
                message = "$college: Update teaching data",
                pull = "--rebase --autostash",
            )
        )
    }
}

fun getGradleCommand(
    college: String,
    statsByProfDir: String = "stats-by-prof",
    outputDir: String = "core/teaching-S25",
): String {
    val rootDir = "../../data/${college.lowercase()}/generated"
    val args = listOf("--teaching", "$rootDir/$outputDir", "$rootDir/$statsByProfDir")
        .joinToString(" ", prefix = "\"", postfix = "\"")
    return "./gradlew colleges:${college.lowercase()}:run --args=$args --scan"
}

teachingDataWorkflow(
    college = "FSU",
    cron = Cron(minute = "0", hour = "16", dayWeek = "2")
)

teachingDataWorkflow(
    college = "USF",
    cron = Cron(minute = "0", hour = "22", dayWeek = "1-5")
)

teachingDataWorkflow(
    college = "TXST",
    cron = Cron(minute = "0", hour = "22", dayWeek = "1-5"),
)

teachingDataWorkflow(
    college = "Rutgers",
    cron = Cron(minute = "0", hour = "22", dayWeek = "1-5"),
    gradleCommand = getGradleCommand(
        "Rutgers",
        statsByProfDir = "stats-by-prof-cleaned",
        outputDir = "core/teaching-S25"
    )
)