package io.github.opletter.courseevals.site.core.states

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.opletter.courseevals.common.data.Semester
import kotlinx.browser.sessionStorage
import org.w3c.dom.get
import org.w3c.dom.set

// NOTE: Unlike for campus & level, we store the semester type in session storage, not local storage.
// We need the value to carry over from the index - and it's reasonable to maintain it during a "session".
// However, we want future visits to not be slowed down by this as a saved option
// In theory it'd be fine to save a value that's more recent than the default - but is that worth it?

class MinSemesterVM<T : Semester<T>>(
    semBounds: Pair<Semester<T>, Semester<T>>,
    private val default: Semester<T>,
    college: String,
    private val updateState: () -> Unit,
    private val getText: (Int) -> String,
) {
    private val storageKey = "course-evals:$college:minSemester"

    val bounds = semBounds.first.numValue to semBounds.second.numValue

    // We use `value` for the actual filter currently applied and `rangeValue` for the value shown on the slider
    // as the user drags it. We only want to actually update the filter when the user releases the slider,

    var value by mutableStateOf(default.numValue)
        private set

    init {
        sessionStorage[storageKey]
            ?.toIntOrNull()
            ?.takeIf { it >= semBounds.first.numValue && it <= semBounds.second.numValue }
            ?.let { value = it }
    }

    var rangeValue by mutableStateOf(value)
        private set

    val showResetButton get() = value != default.numValue
    val text get() = getText(rangeValue)

    fun setRangeValue(num: Number?) {
        num?.let { rangeValue = it.toInt() }
    }

    fun setValue(num: Number?) {
        num?.let {
            value = it.toInt()
            sessionStorage[storageKey] = value.toString()
            updateState()
        }
    }

    fun reset() {
        setValue(default.numValue)
        setRangeValue(default.numValue)
    }
}