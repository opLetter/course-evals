package io.github.opletter.courseevals.site.core.states

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.opletter.courseevals.site.core.misc.SemesterConfig
import kotlinx.browser.sessionStorage
import org.w3c.dom.get
import org.w3c.dom.set

// NOTE: Unlike for campus & level, we store the semester type in session storage, not local storage.
// We need the value to carry over from the index - and it's reasonable to maintain it during a "session".
// However, we want future visits to not be slowed down by this as a saved option
// In theory it'd be fine to save a value that's more recent than the default - but is that worth it?

class MinSemesterVM(
    private val config: SemesterConfig<*>,
    key: String,
    private val refreshState: () -> Unit,
) {
    private val storageKey = "course-evals:$key:minSemester"

    val bounds = config.bounds.run { first.numValue to second.numValue }
    val default = config.default.numValue

    // We use `value` for the actual filter currently applied and `rangeValue` for the value shown on the slider
    // as the user drags it. We only want to actually update the filter when the user releases the slider,

    var value by mutableStateOf(default)
        private set

    init {
        sessionStorage[storageKey]
            ?.toIntOrNull()
            ?.takeIf { it in bounds.first..bounds.second }
            ?.let { value = it }
    }

    fun getText(num: Number) = config.builder(num.toInt()).toString()

    fun setValue(num: Number?) {
        num?.let {
            value = it.toInt()
            sessionStorage[storageKey] = value.toString()
            refreshState()
        }
    }
}