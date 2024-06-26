package io.github.opletter.courseevals.site.core.states

import androidx.compose.runtime.*
import io.github.opletter.courseevals.common.data.InstructorStats
import io.github.opletter.courseevals.common.data.getRatingAve
import io.github.opletter.courseevals.common.data.prepend
import io.github.opletter.courseevals.site.core.misc.toFixed

@Stable
class ProfSummaryVM(
    statsByProf: Map<String, InstructorStats>,
    selectedProf: String,
    teachingCourses: List<String>,
    defaultQuestion: Int,
    val getGraphLabel: (Int) -> String,
    getUrl: (String?) -> String,
    getUrlText: (String?) -> String,
    navigateTo: (String?) -> Unit,
) {
    private val profStats = statsByProf[selectedProf] ?: error("Prof not found ($selectedProf)")
    private val courses = profStats.courseStats.keys.sorted().prepend("All Courses")

    val coursesToDisplay = courses.associateWith { it in teachingCourses }

    var selectedQ by mutableStateOf(defaultQuestion)
    var selectedCourse by mutableStateOf(0)

    val graphNums: List<Int>
        get() {
            val stats = if (selectedCourse == 0) profStats.overallStats
            else profStats.courseStats[courses[selectedCourse]] ?: error("Course not found ($selectedCourse)")

            return stats[selectedQ]
        }
    val graphLabel get() = getGraphLabel(selectedQ)

    val average get() = graphNums.getRatingAve().toFixed(2)
    val numResponses get() = graphNums.sum()

    private val deptStats by derivedStateOf {
        statsByProf.values.map { it.overallStats[selectedQ].getRatingAve() }
    }
    private val courseStats by derivedStateOf {
        if (selectedCourse == 0) return@derivedStateOf null
        val course = courses[selectedCourse]
        statsByProf.values.mapNotNull { stats ->
            stats.courseStats[course]?.let { it[selectedQ].getRatingAve() }
        }
    }
    val aveComparison by derivedStateOf {
        val selectedCourseOrNull = courses[selectedCourse].takeIf { selectedCourse != 0 }
        AveComparisonData(
            average = (courseStats ?: deptStats).average().toFixed(2),
            totalNum = (courseStats ?: deptStats).size.toString(),
            url = getUrl(selectedCourseOrNull),
            urlText = getUrlText(selectedCourseOrNull),
            onLinkClick = { navigateTo(selectedCourseOrNull) },
        )
    }
}

class AveComparisonData(
    val average: String,
    val totalNum: String,
    val url: String,
    val urlText: String,
    val onLinkClick: () -> Unit,
)