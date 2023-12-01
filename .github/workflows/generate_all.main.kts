#!/usr/bin/env kotlin
@file:DependsOn("io.github.typesafegithub:github-workflows-kt:1.6.0")
@file:Import("common_setup.main.kts")

import io.github.typesafegithub.workflows.actions.peterevans.CreatePullRequestV5
import io.github.typesafegithub.workflows.domain.RunnerType.UbuntuLatest
import io.github.typesafegithub.workflows.domain.triggers.WorkflowDispatch
import io.github.typesafegithub.workflows.dsl.expressions.Contexts
import io.github.typesafegithub.workflows.dsl.expressions.expr
import io.github.typesafegithub.workflows.dsl.workflow
import io.github.typesafegithub.workflows.yaml.writeToFile
import kotlin.properties.ReadOnlyProperty

// region general definitions
// Trying a possible solution to https://github.com/typesafegithub/github-workflows-kt/issues/811
// made more general than necessary as a proof of concept for other values
class ExprResult<T>(val value: T, internal val propertyName: String, internal val stringValue: String)

class ExprResultDelegate<T>(private val _path: String) {
    operator fun invoke(input: T): ReadOnlyProperty<Any?, ExprResult<T>> {
        return ReadOnlyProperty { _, property ->
            ExprResult(input, property.name, "$_path.${property.name}")
        }
    }
}

fun expr(expression: Contexts.() -> ExprResult<*>): String = with(Contexts) { expr(expression().stringValue) }

@Suppress("UnusedReceiverParameter")
val Contexts.inputs get() = ExprResultDelegate<WorkflowDispatch.Input>("inputs")

fun WorkflowDispatch(vararg inputs: ExprResult<WorkflowDispatch.Input>) =
    WorkflowDispatch(inputs.associate { it.propertyName to it.value })

// endregion

val college by Contexts.inputs(
    WorkflowDispatch.Input(
        description = "The college to generate data for",
        required = true,
        type = WorkflowDispatch.Type.Choice,
        options = listOf("FSU", "USF", "TXST")
    )
)

workflow(
    name = "Generate All Data",
    on = listOf(
        WorkflowDispatch(college),
    ),
    sourceFile = __FILE__.toPath(),
    targetFileName = "generate_all_data.yml"
) {

    job(id = "gen_and_pr", runsOn = UbuntuLatest) {
        setUpWithData()

        val college = expr { college }
        run {
            val rootDir = "../../data/$college"
            val args = listOf("--write-all", "$rootDir/generated", "$rootDir/raw/reports")
                .joinToString(" ", prefix = "\"", postfix = "\"")
            val gradleCommand = "./gradlew colleges:$college:run --args=$args --scan"
            run(name = "Run", command = gradleCommand)
        }

        uses(
            name = "Create Pull Request",
            action = CreatePullRequestV5(
                token = io.github.typesafegithub.workflows.dsl.expressions.expr { EVALS_DATA_TOKEN },
                path = "data",
                commitMessage = "$college: Generate all data",
                branch = "generate-all-data/$college",
                title = "$college: Generate all data",
                deleteBranch = true,
                body = """
                    Auto-generated changes from [course-evals][1].
                    PR created by [create-pull-request][2].
                    
                    [1]: https://github.com/opLetter/course-evals/actions/workflows/generate_all_data.yml
                    [2]: https://github.com/peter-evans/create-pull-request
                """.trimIndent()
            )
        )
    }

}.writeToFile(addConsistencyCheck = false)