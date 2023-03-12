package io.github.opletter.courseevals.site.core.states

abstract class CheckmarksVM<T>(val updateState: () -> Unit) {
    abstract val checks: Map<T, Boolean>

    val selected get() = checks.filterValues { it }.keys
    val onlyOneChecked get() = selected.size == 1

    protected abstract fun handleClick(data: T)

    fun click(data: T, updateState: Boolean = true) {
        handleClick(data)
        if (updateState) updateState()
    }
}