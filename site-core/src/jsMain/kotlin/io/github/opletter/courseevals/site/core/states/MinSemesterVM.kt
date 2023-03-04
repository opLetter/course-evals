package io.github.opletter.courseevals.site.core.states

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.opletter.courseevals.common.data.Semester
import io.github.opletter.courseevals.common.data.SemesterType
import kotlinx.browser.sessionStorage
import org.w3c.dom.get
import org.w3c.dom.set

// NOTE: Unlike for campus & level, we store the semester type in session storage, not local storage.
// We need the value to carry over from the index - and it's reasonable to maintain it during a "session".
// However, we want future visits to not be slowed down by this as a saved option
// In theory it'd be fine to save a value that's more recent than the default - but is that worth it?
private const val MIN_SEMESTER_KEY = "course-evals:rutgers:minSemester"

class MinSemesterVM {
    val bounds = Semester(SemesterType.Spring, 2014).numValue to
            Semester(SemesterType.Spring, 2022).numValue

    // Unsure about what to choose for this default value.
    // Ideally it'd be as recent as possible (for page loading speed), but not too recent (for relevance)
    // Chosen for now to be the 5th semester back (from which we have data)
    // Considered making it the first semester of current-year seniors, but that may slow down pages too much
    // for data that most people wouldn't want to see.
    private val default = Semester(SemesterType.Spring, 2020)

    // We store two separate mutable states
    // "value" for the actual filter used and "rangeValue" for the value shown on the slider
    // This is because we only want to actually update the filter when the user releases the slider,
    // but we want to show the value on the slider as the user drags it

    var value by mutableStateOf(default.numValue)
        private set

    init {
        sessionStorage[MIN_SEMESTER_KEY]
            ?.toIntOrNull()
            ?.takeIf { it >= bounds.first && it <= bounds.second }
            ?.let { value = it }
    }

    var rangeValue by mutableStateOf(value)
        private set

    val showResetButton get() = value != default.numValue
    val text get() = "${if (rangeValue % 2 == 0) "Spring" else "Fall"} ${rangeValue / 2}"

    fun setRangeValue(num: Number?) = num?.let { rangeValue = it.toInt() }

    fun setValue(num: Number?) = num?.let {
        value = it.toInt()
        sessionStorage[MIN_SEMESTER_KEY] = value.toString()
    }

    fun reset() {
        setValue(default.numValue)
        setRangeValue(default.numValue)
    }
}