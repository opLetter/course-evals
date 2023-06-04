package io.github.opletter.courseevals.site.core.states

import androidx.compose.runtime.*
import io.github.opletter.courseevals.common.data.*
import io.github.opletter.courseevals.common.remote.WebsiteDataSource
import io.github.opletter.courseevals.site.core.misc.College
import io.github.opletter.courseevals.site.core.misc.None
import io.github.opletter.courseevals.site.core.misc.SchoolStrategy
import io.github.opletter.courseevals.site.core.misc.jsFormatNum
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

@Stable
class DataPageVM(
    private val coroutineScope: CoroutineScope,
    val college: College,
    urlParams: Map<String, String>,
) {
    private val repository: WebsiteDataSource = college.dataSource

    val urlPrefix = when {
        college is College.Rutgers && college.fake -> "fake"
        else -> ""
    }

    var navState by mutableStateOf(NavState())
        private set

    private lateinit var globalData: GlobalData
    private var deptData by mutableStateOf(DeptData())

    private var initialLoading by mutableStateOf(true)
    var pageLoading by mutableStateOf(false)

    private val refreshNavState: () -> Unit = {
        println("refreshing")
        with(navState) {
            selectSchool(
                school = school.selected,
                dept = dept.selected,
                course = course.selected,
                prof = prof.selected,
            )
        }
    }
    val minSemVM = MinSemesterVM(college.semesterOptions, college.urlPath, refreshNavState)
    val campusVM = CampusVM(college.campuses, college.urlPath, refreshNavState)
    val levelOfStudyVM = LevelOfStudyVM(refreshNavState)

    private val statsByProf by derivedStateOf {
        deptData.allStatsByProf.filter {
            it.value.lastSem >= minSemVM.value || deptData.teachingMap.containsKey(it.key)
        }
    }

    val url: String get() = getUrl()

    val state: State by derivedStateOf {
        when {
            initialLoading -> State.InitialLoading

            !navState.prof.selected.isBlankOrNone() -> {
                val profSummaryVM = ProfSummaryVM(
                    statsByProf = statsByProf,
                    selectedProf = navState.prof.selected,
                    teachingCourses = deptData.teachingMap[navState.prof.selected].orEmpty(),
                    defaultQuestion = college.questions.defaultIndex,
                    getGraphLabel = college.questions.getGraphLabel,
                    getUrl = { getUrl(prof = None, course = it ?: None) },
                    getUrlText = { getCode(course = it ?: None) },
                    navigateTo = { if (it == null) selectProf(None) else selectCourse(it) }
                )
                State.Prof(profSummaryVM)
            }

            !navState.course.selected.isBlankOrNone() -> {
                val mapToDisplay = statsByProf
                    .mapNotNull { (prof, stats) ->
                        // Include profs that are teaching course but don't have stats for it - use their overall stats
                        // using "[]" prefix before name to denote such prof so that it can be identified in the UI
                        // not ideal solution but involves minimal changes to existing code structure
                        val profStats = stats.courseStats[navState.course.selected]
                        when {
                            profStats != null -> prof to profStats
                            prof in teachingInstructors -> "[]$prof" to stats.overallStats
                            else -> null
                        }
                    }.toMap().toDisplayMap()
                State.Course(mapToDisplay)
            }

            else -> {
                val mapToDisplay = statsByProf.mapValues { it.value.overallStats }.toDisplayMap()
                State.Dept(mapToDisplay)
            }
        }
    }

    private val activeSchoolsByCode by derivedStateOf {
        globalData.schoolsByCode.filterValues {
            it.campuses.intersect(campusVM.selected).isNotEmpty() && it.level in levelOfStudyVM.selected
        }
    }
    private val schoolList
        get() = if (college.schoolStrategy == SchoolStrategy.SHOW_ALL) globalData.schoolsByCode.values
        else activeSchoolsByCode.values

    val teachingInstructors: List<String>
        get() = deptData.teachingMap[navState.course.selected].orEmpty()

    val pageTitle: String
        get() {
            return when (state) {
                is State.InitialLoading -> ""
                is State.Prof -> "${navState.prof.selected} (${getCode()})"
                is State.Dept -> globalData.deptMap[navState.dept.selected]?.let { "$it (${getCode()})" }
                    ?: error("Invalid dept selected (${navState.dept.selected})")

                is State.Course -> deptData.courseNames[navState.course.selected]
                    ?.let { "$it (${getCode()})" } ?: getCode()
            }
        }

    private fun courseWithName(code: String): String = deptData.courseNames[code]?.let { "$it ($code)" } ?: code
    val courseAsName: String get() = courseWithName(navState.course.selected)
    val coursesWithNames: List<String> by derivedStateOf {
        navState.course.list.map { courseWithName(it) }
    }

    @Stable
    inner class SearchBarVM {
        var searchBoxInput by mutableStateOf("")
        var searchBarClickedOnce by mutableStateOf(false)
        var searchEnterHandled by mutableStateOf(true)
        val searchBarPlaceholder = college.searchHint

        val searchBarSuggestions by derivedStateOf {
            // to not slow down initial page load, add suggestions to DataList after search bar is initially clicked
            if (!searchBarClickedOnce) emptyList()
            else {
                val searchableProfs = globalData.allInstructors
                    .filterKeys { it in activeSchoolsByCode.keys }
                    .flatMap { (school, profs) ->
                        profs.mapNotNull { prof ->
                            if (prof.latestSem < minSemVM.value) null
                            else "${prof.name} (${getCode(school = school, dept = prof.dept, course = None)})"
                        }
                    }
                searchableDepts + searchableProfs
            }
        }

        private val searchableDepts by derivedStateOf {
            activeSchoolsByCode.flatMap { (code, school) ->
                school.depts.map {
                    "${getCode(school = code, dept = it, course = None)} - ${globalData.deptMap[it]}"
                }
            }
        }

        fun valueTransform(value: String): String = college.searchValueTransform(value)

        fun onEnterSearch() {
            searchEnterHandled = false
            val input = searchBoxInput
            searchBoxInput = ""

            when (college) {
                is College.Rutgers -> {
                    // valid states: "SMITH, JOHN (01:198)", "01:198 - Computer Science ", "01:198", "01:198:112"
                    val school = input.substringAfterBefore("(", ":")
                    val dept = input.substringAfterBefore(":", ")")

                    if (input.contains("(")) { // assuming no depts have "(" in them - dangerous/wrong?
                        val prof = input.substringBefore(" (")
                        selectSchool(school = school, dept = dept, prof = prof)
                    } else {
                        val course = dept.substringAfter(":", None)
                        val newDept = dept.substringBefore(":").substringBefore(" ")
                            .also { if (activeSchoolsByCode[school]?.depts?.contains(it) != true) return }
                        selectSchool(school = school, dept = newDept, course = course)
                    }
                }

                is College.FSU, College.USF -> {
                    if ("(" in input) {
                        selectDept(
                            dept = input.substringAfterBefore("(", ")"),
                            prof = input.substringBefore(" ("),
                        )
                    } else {
                        val dept = input.take(3)
                            .also { if (it !in activeSchoolsByCode.values.first().depts) return }
                        selectDept(dept = dept, course = input.drop(3))
                    }
                }
            }
        }
    }

    lateinit var searchBarVM: SearchBarVM
        private set

    fun selectSchool(
        school: String?,
        dept: String? = null,
        course: String? = null,
        prof: String? = null,
    ) {
        val newSchool = activeSchoolsByCode[school] ?: activeSchoolsByCode.values.first()
        if (college.schoolStrategy == SchoolStrategy.SHOW_ALL)
            campusVM.selectOnly(Campus.valueOf(newSchool.code.uppercase()))
        val newDept = dept.takeIf { it in newSchool.depts } ?: newSchool.depts.first()
        selectDept(dept = newDept, school = newSchool, course = course, prof = prof)
    }

    fun selectDept(
        dept: String,
        school: School = activeSchoolsByCode[navState.school.selected]
            ?: error("Selected school not found (${navState.school.selected})"),
        course: String? = null,
        prof: String? = null,
    ) {
        val profsChanged = navState.prof.list.size != (statsByProf.size + 1) // (+1 for None)
        if (!isSameAsState(school.code, dept, course, prof) && !profsChanged) {
            // needed if campus is changed but currently selected school is still valid
            if (activeSchoolsByCode.keys != navState.school.list)
                navState = navState.copy(school = navState.school.copy(list = schoolList))
            return
        }
        pageLoading = true
        coroutineScope.launch {
            // prevent unnecessary network requests & calculations
            val newDept = navState.school.selected != school.code || navState.dept.selected != dept
            if (newDept) {
                val allStatsByProf = async { repository.getStatsByProf(school.code, dept) }
                val courseNames = async { repository.getCourseNamesOrEmpty(school.code, dept) }
                val teachingMap = async { repository.getTeachingDataOrEmpty(school.code, dept) }
                deptData = DeptData(
                    teachingMap = teachingMap.await(),
                    courseNames = courseNames.await(),
                    allStatsByProf = allStatsByProf.await(),
                )
            }

            val profs = if (newDept || profsChanged)
                statsByProf.keys.sorted().prepend(None)
            else navState.prof.list

            val courses = if (newDept || profsChanged) // if profs changed, courses may have changed too
                statsByProf.values.flatMap { it.courseStats.keys }
                    .plus(deptData.teachingMap.keys.filter { it[0].isDigit() }) // add only courses from teachingMap
                    .toSet().sorted().prepend(None)
            else navState.course.list

            val courseValid = course != None && course in courses
            val oldState = navState
            navState = navState.copy(
                school = navState.school.copy(schoolList, school.code),
                dept = navState.dept.copy(school.associateDeptsToName(), dept),
                course = navState.course.copy(courses, course.takeIf { courseValid } ?: None),
                prof = navState.prof.copy(profs, prof.takeIf { !courseValid && it in profs } ?: None),
            )

            initialLoading = false
            if (oldState == navState) pageLoading = false // must be done manually since screen won't recompose
        }
    }

    fun selectCourse(course: String) {
        val courseCode = course.substringAfterLast('(').substringBefore(')')
        navState = navState.copy(
            course = navState.course.copy(selected = courseCode),
            prof = navState.prof.copy(selected = None)
        )
    }

    fun selectProf(prof: String) {
        navState = navState.copy(
            prof = navState.prof.copy(selected = prof),
            course = navState.course.copy(selected = None)
        )
    }

    init {
        coroutineScope.launch {
            val allInstructors = async { repository.getAllInstructors() }
            val deptMap = async { repository.getDeptMap() }
            val schoolsByCode = async { repository.getSchoolMap() }
            globalData = GlobalData(
                schoolsByCode = schoolsByCode.await(),
                deptMap = deptMap.await(),
                allInstructors = allInstructors.await(),
            )
            searchBarVM = SearchBarVM()

            val school = urlParams["school"]
            enableSchoolIfDisabled(school)

            selectSchool(
                school = school,
                dept = urlParams["dept"],
                course = urlParams["course"],
                prof = urlParams["prof"]?.decodeURL(),
            )
        }
    }

    fun onPopState(paramsUrl: String) {
        val params = paramsUrl.drop(1).split('&').associate {
            it.split('=', limit = 2).zipWithNext().getOrNull(0) ?: return
        }
        val school = params["school"]
            ?: if (college.schoolStrategy != SchoolStrategy.SINGLE) return else navState.school.selected
        val dept = params["dept"] ?: return
        val course = params["course"]
        val prof = params["prof"]?.decodeURL()

        enableSchoolIfDisabled(school)

        // need to figure out how to deal wih invalid course/prof on pop -maybe?
        if (school != navState.school.selected) {
            selectSchool(school = school, dept = dept, course = course, prof = prof)
        } else if (dept != navState.dept.selected) {
            selectDept(dept = dept, course = course, prof = prof)
        } else if (course != null) {
            selectCourse(course)
        } else if (prof != null) {
            selectProf(prof)
        } else if (navState.course.selected != None) {
            selectCourse(None)
        } else if (navState.prof.selected != None) {
            selectProf(None)
        }
    }

    private fun School.associateDeptsToName(includeCode: Boolean = true): List<Pair<String, String>> {
        return depts.associateWith { code ->
            globalData.deptMap[code]?.let { name -> if (includeCode) "$code - $name" else name }
                ?: error("Invalid dept while associating to names ($code)")
        }.toList()
    }

    // do what's needed to enable the school if it's disabled
    private fun enableSchoolIfDisabled(school: String?) {
        globalData.schoolsByCode[school]
            ?.takeIf { it.code !in activeSchoolsByCode.keys }
            ?.let {
                // choose only first campus if many work - no need for others
                // don't update state as it will be updated anyway after this functions
                // and updating state here as well could cause some race conditions
                if (it.campuses.intersect(campusVM.selected).isEmpty())
                    campusVM.click(it.campuses.first(), false)
                if (it.level !in levelOfStudyVM.selected)
                    levelOfStudyVM.click(it.level)
            }
    }
}

private fun DataPageVM.isSameAsState(school: String, dept: String, course: String?, prof: String?): Boolean {
    return school != navState.school.selected ||
            dept != navState.dept.selected ||
            (course ?: None) != navState.course.selected ||
            (prof ?: None) != navState.prof.selected
}

private fun DataPageVM.getCode(
    school: String = navState.school.selected,
    dept: String = navState.dept.selected,
    course: String = navState.course.selected,
): String = college.getCode(school, dept, course.let { if (it.isBlankOrNone()) "" else it })

private fun DataPageVM.getUrl(
    school: String = navState.school.selected,
    dept: String = navState.dept.selected,
    course: String = navState.course.selected,
    prof: String = navState.prof.selected,
): String {
    return "?" +
            (if (college.schoolStrategy != SchoolStrategy.SINGLE) "school=$school&" else "") +
            "dept=$dept" +
            with(course) { if (isBlankOrNone()) "" else "&course=$this" } +
            with(prof) { if (isBlankOrNone()) "" else "&prof=${encodeURL()}" }
}

fun DataPageVM.getProfUrl(prof: String): String = getUrl(course = None, prof = prof)

private fun Map<String, Ratings>.toDisplayMap(addAverage: Boolean = true): Map<String, List<String>> {
    return mapValues { (_, ratings) -> ratings.getAvesAndTotal() }
        .let { if (!addAverage || it.size <= 1) it else it.plus("Average" to it.getAveStats()) }
        .mapValues { (_, stats) ->
            stats.ratings.map { jsFormatNum(num = it, decDigits = 1) } + stats.numResponses.toString()
        }
}

private class GlobalData(
    val schoolsByCode: Map<String, School>,
    val deptMap: Map<String, String>,
    val allInstructors: Map<String, List<Instructor>>,
)

private class DeptData(
    val teachingMap: Map<String, List<String>> = emptyMap(),
    val courseNames: Map<String, String> = emptyMap(),
    val allStatsByProf: Map<String, InstructorStats> = emptyMap(),
)

data class DropDownState<T>(
    val list: Collection<T> = emptyList(),
    val selected: String = "",
)

data class NavState(
    val school: DropDownState<School> = DropDownState(),
    val dept: DropDownState<Pair<String, String>> = DropDownState(),
    val course: DropDownState<String> = DropDownState(),
    val prof: DropDownState<String> = DropDownState(),
)

data class Questions(
    val full: List<String>,
    val short: List<String>,
    val defaultIndex: Int,
    val getGraphLabel: (Int) -> String,
)

private fun String.isBlankOrNone(): Boolean = isBlank() || equals(None)

private val urlEncodings = listOf("," to "%2C", " " to "%20", "." to "%2E", "'" to "%27")
private fun String.encodeURL(): String = urlEncodings.fold(this) { acc, (a, b) -> acc.replace(a, b) }
private fun String.decodeURL(): String = urlEncodings.fold(this) { acc, (a, b) -> acc.replace(b, a) }