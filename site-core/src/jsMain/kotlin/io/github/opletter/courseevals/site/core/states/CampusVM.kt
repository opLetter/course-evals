package io.github.opletter.courseevals.site.core.states

import androidx.compose.runtime.mutableStateMapOf
import io.github.opletter.courseevals.common.data.Campus
import kotlinx.browser.localStorage
import org.w3c.dom.get
import org.w3c.dom.set

class CampusVM(
    private val campuses: Map<Campus, Boolean>,
    key: String,
    refreshState: () -> Unit,
) : CheckmarksVM<Campus>(refreshState) {
    private val campusKey = "course-evals:$key:campuses"

    private val checksState = mutableStateMapOf<Campus, Boolean>()
        .apply {
            campuses.forEach { this[it.key] = it.value }
        }

    init {
        localStorage[campusKey].orEmpty()
            .split(",")
            .map { it.toBoolean() }
            .takeIf { it.size == campuses.size && true in it }
            ?.forEachIndexed { index, b ->
                checksState[campuses.keys.elementAt(index)] = b
            }
    }

    override val checks
        get() = campuses.keys.associateWith { checksState[it]!! } // recreate map so that key order is maintained

    override fun handleClick(data: Campus) {
        checksState[data] = !(checksState[data] ?: error("No value for $data"))
        localStorage[campusKey] = checks.values.joinToString(",")
    }

    fun selectOnly(campus: Campus) {
        checksState.forEach { checksState[it.key] = false }
        checksState[campus] = true
        localStorage[campusKey] = checks.values.joinToString(",")
    }
}