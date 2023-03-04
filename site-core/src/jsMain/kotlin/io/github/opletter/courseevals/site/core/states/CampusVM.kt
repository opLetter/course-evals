package io.github.opletter.courseevals.site.core.states

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.opletter.courseevals.common.data.Campus
import kotlinx.browser.localStorage
import org.w3c.dom.get
import org.w3c.dom.set

private const val CAMPUS_KEY = "course-evals:rutgers:campuses"

class CampusVM(updateState: () -> Unit) : CheckmarksVM<Campus>(updateState) {
    private var checkedNB by mutableStateOf(true)
    private var checkedCM by mutableStateOf(true)
    private var checkedNK by mutableStateOf(true)

    init {
        localStorage[CAMPUS_KEY].orEmpty()
            .split(",")
            .map { it.toBoolean() }
            .takeIf { it.size == 3 && true in it }
            ?.let {
                checkedNB = it[0]
                checkedCM = it[1]
                checkedNK = it[2]
            }
    }

    override val checks
        get() = mapOf(Campus.NB to checkedNB, Campus.CM to checkedCM, Campus.NK to checkedNK)

    override fun handleClick(data: Campus) {
        when (data) {
            Campus.NB -> checkedNB = !checkedNB
            Campus.CM -> checkedCM = !checkedCM
            Campus.NK -> checkedNK = !checkedNK
        }
        localStorage[CAMPUS_KEY] = checks.values.joinToString(",")
    }
}