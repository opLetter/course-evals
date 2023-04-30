package io.github.opletter.courseevals.site.core.states

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.opletter.courseevals.common.data.LevelOfStudy
import kotlinx.browser.localStorage
import org.w3c.dom.get
import org.w3c.dom.set

private const val LEVEL_OF_STUDY_KEY = "course-evals:rutgers:levelOfStudy"

class LevelOfStudyVM(refreshState: () -> Unit) : CheckmarksVM<LevelOfStudy>(refreshState) {
    private var checkedUndergrad by mutableStateOf(true)
    private var checkedGrad by mutableStateOf(true)

    init {
        localStorage[LEVEL_OF_STUDY_KEY].orEmpty()
            .split(",")
            .map { it.toBoolean() }
            .takeIf { it.size == 2 && true in it }
            ?.let {
                checkedUndergrad = it[0]
                checkedGrad = it[1]
            }
    }

    override val checks get() = mapOf(LevelOfStudy.U to checkedUndergrad, LevelOfStudy.G to checkedGrad)

    override fun handleClick(data: LevelOfStudy) {
        when (data) {
            LevelOfStudy.U -> checkedUndergrad = !checkedUndergrad
            LevelOfStudy.G -> checkedGrad = !checkedGrad
        }
        localStorage[LEVEL_OF_STUDY_KEY] = checks.values.joinToString(",")
    }
}