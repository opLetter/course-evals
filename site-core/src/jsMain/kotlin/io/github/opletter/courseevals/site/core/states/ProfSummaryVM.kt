package io.github.opletter.courseevals.site.core.states

import androidx.compose.runtime.*
import io.github.opletter.courseevals.common.data.InstructorStats
import io.github.opletter.courseevals.common.data.getRatingAve
import io.github.opletter.courseevals.common.data.getRatingStats
import io.github.opletter.courseevals.common.data.prepend
import io.github.opletter.courseevals.site.core.misc.jsFormatNum

@Stable
class ProfSummaryVM(
    statsByProf: Map<String, InstructorStats>,
    selectedProf: String,
    deptCode: String,
    deptUrl: String,
    teachingCourses: List<String>,
    getCourseUrl: (String) -> String,
    goToDeptData: () -> Unit,
    goToCourseData: (String) -> Unit,
) {
    private val profStats = statsByProf[selectedProf]
        ?: error("Prof not found ($selectedProf)")

    var selectedQ by mutableStateOf(7)

    var selectedCourse by mutableStateOf(0)
    private val courses = profStats.courseStats.keys
        .distinct().sorted().prepend("All Courses")
    val coursesToDisplay = courses.associateWith { it in teachingCourses }

    val graphNums by derivedStateOf {
        val stats = if (selectedCourse == 0) {
            profStats.overallStats
        } else {
            profStats.courseStats[courses[selectedCourse]]
                ?: error("Course not found ($selectedCourse)")
        }
        stats[selectedQ]
    }
    val graphLabel get() = if (selectedQ < 7) "Disagree -> Agree" else "Poor -> Excellent"

    val average get() = jsFormatNum(num = graphNums.getRatingAve(), decDigits = 2)
    val numResponses get() = graphNums.sum()

    private val deptAveComparison by derivedStateOf {
        val deptAves = statsByProf.values.map { it.overallStats[selectedQ].getRatingAve() }.average()
        AveComparisonData(
            average = jsFormatNum(num = deptAves, decDigits = 2),
            totalNum = statsByProf.size.toString(),
            url = deptUrl,
            urlText = deptCode,
            onLinkClick = { goToDeptData() }
        )
    }

    private val courseAveComparison by derivedStateOf {
        val course = courses[selectedCourse].takeIf { selectedCourse != 0 } ?: return@derivedStateOf null
        val courseAve = statsByProf.values.mapNotNull { stats ->
            stats.courseStats[course]?.let { it[selectedQ].getRatingStats().first }
        }
        AveComparisonData(
            average = jsFormatNum(num = courseAve.average(), decDigits = 2),
            totalNum = courseAve.size.toString(),
            url = getCourseUrl(course),
            urlText = "$deptCode:$course",
            onLinkClick = { goToCourseData(course) }
        )
    }

    val aveComparison get() = courseAveComparison ?: deptAveComparison
}

data class AveComparisonData(
    val average: String,
    val totalNum: String,
    val url: String,
    val urlText: String,
    val onLinkClick: () -> Unit,
)