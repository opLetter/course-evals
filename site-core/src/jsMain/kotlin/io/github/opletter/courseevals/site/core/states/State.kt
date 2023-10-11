package io.github.opletter.courseevals.site.core.states

sealed interface State {
    sealed interface Loaded : State

    sealed interface TableData : Loaded {
        val mapToDisplay: Map<String, List<String>>
    }

    data object InitialLoading : State
    class Dept(override val mapToDisplay: Map<String, List<String>>) : TableData
    class Course(override val mapToDisplay: Map<String, List<String>>) : TableData
    class Prof(val profSummaryVM: ProfSummaryVM) : Loaded
}