#!/usr/bin/env kotlin
@file:DependsOn("io.github.typesafegithub:github-workflows-kt:1.9.0")

import io.github.typesafegithub.workflows.domain.actions.Action
import io.github.typesafegithub.workflows.domain.actions.RegularAction

class ReleaseDownloaderV1(
    private val repository: String,
    private val tag: String,
    private val fileName: String,
    private val tarBall: Boolean,
    private val zipBall: Boolean,
    private val extract: Boolean,
) : RegularAction<Action.Outputs>("robinraju", "release-downloader", "v1.8") {
    override fun toYamlArguments() = linkedMapOf(
        "repository" to repository,
        "tag" to tag,
        "fileName" to fileName,
        "tarBall" to tarBall.toString(),
        "zipBall" to zipBall.toString(),
        "extract" to extract.toString(),
    )

    override fun buildOutputObject(stepId: String) = Outputs(stepId)
}

class UploadPagesArtifactV2(private val path: String) :
    RegularAction<Action.Outputs>("actions", "upload-pages-artifact", "v2") {
    override fun toYamlArguments() = linkedMapOf("path" to path)

    override fun buildOutputObject(stepId: String) = Outputs(stepId)
}

class DeployPagesV2 : RegularAction<Action.Outputs>("actions", "deploy-pages", "v2") {
    override fun toYamlArguments() = linkedMapOf<String, String>()

    override fun buildOutputObject(stepId: String) = Outputs(stepId)
}