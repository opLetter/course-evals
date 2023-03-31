package io.github.opletter.courseevals.site.core.states

import androidx.compose.runtime.*
import io.github.opletter.courseevals.common.data.*
import io.github.opletter.courseevals.common.remote.WebsiteDataSource
import io.github.opletter.courseevals.site.core.misc.College
import io.github.opletter.courseevals.site.core.misc.None
import io.github.opletter.courseevals.site.core.misc.jsFormatNum
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

@Stable
class DataPageVM(
    private val repository: WebsiteDataSource,
    private val coroutineScope: CoroutineScope,
    val college: College,
    urlParams: Map<String, String>,
) {
    val urlPrefix = when {
        college is College.Rutgers && college.fake -> "fake"
        else -> ""
    }

    var state: DataPageState by mutableStateOf(DataPageState())
        private set

    private lateinit var globalData: GlobalData
    private var deptData by mutableStateOf(DeptData())

    private var initialLoading by mutableStateOf(true)
    var pageLoading by mutableStateOf(false)

    private val updateState: () -> Unit = {
        with(state) {
            selectSchool(
                school = school.selected,
                dept = dept.selected,
                course = course.selected,
                prof = prof.selected,
            )
        }
    }
    val minSemVM = MinSemesterVM(college.semesterOptions, college.urlPath, updateState)
    val campusVM = CampusVM(college.campuses, college.urlPath, updateState)
    val levelOfStudyVM = LevelOfStudyVM(updateState)

    private val statsByProf by derivedStateOf {
        deptData.allStatsByProf.filter {
            it.value.lastSem >= minSemVM.value || deptData.teachingMap.containsKey(it.key)
        }
    }

    val url: String get() = getUrl()

    val status
        get() = when {
            // schoolMap.isEmpty() can work for initialLoading but doesn't account for dept/prof loading
            initialLoading -> Status.InitialLoading
            !state.prof.selected.isBlankOrNone() -> Status.Prof
            !state.course.selected.isBlankOrNone() -> Status.Course
            else -> Status.Dept
        }

    val mapToDisplay: Map<String, List<String>>? by derivedStateOf {
        when (status) {
            Status.Dept -> statsByProf.mapValues { it.value.overallStats }.toDisplayMap()
            Status.Course -> statsByProf
                .mapNotNull { (prof, stats) ->
                    // Include profs that are teaching course but don't have stats for it - use their overall stats
                    // using "[]" prefix before name to denote such prof so that it can be identified in the UI
                    // not ideal solution but involves minimal changes to existing code structure
                    val profStats = stats.courseStats[state.course.selected]
                    when {
                        profStats != null -> prof to profStats
                        prof in teachingInstructors -> "[]$prof" to stats.overallStats
                        else -> null
                    }
                }.toMap().toDisplayMap()

            else -> null
        }
    }

    private val activeSchoolsByCode by derivedStateOf {
        globalData.schoolsByCode.filterValues {
            it.campuses.intersect(campusVM.selected).isNotEmpty() && it.level in levelOfStudyVM.selected
        }
    }
    private val schoolList
        get() = if (college.showFullSchoolList) globalData.schoolsByCode.values else activeSchoolsByCode.values

    val teachingInstructors: List<String>
        get() = deptData.teachingMap[state.course.selected].takeIf { status == Status.Course }.orEmpty()

    val pageTitle: String
        get() {
            return when (status) {
                Status.InitialLoading -> ""
                Status.Prof -> with(state) { "${prof.selected} (${getCode()})" }
                Status.Dept -> globalData.deptMap[state.dept.selected]?.let { "$it (${getCode()})" }
                    ?: error("Invalid dept (${state.dept.selected}")

                else -> deptData.courseNames[state.course.selected]
                    ?.let { "$it (${getCode()})" } ?: getCode()
            }
        }

    val courseAsName: String
        get() = state.course.selected.let { code ->
            deptData.courseNames[code]?.let { "$it ($code)" } ?: code
        }
    val coursesWithNames: List<String> by derivedStateOf {
        state.course.list.map { code ->
            deptData.courseNames[code]?.let { "$it ($code)" } ?: code
        }
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

        fun valueTransform(value: String): String = when (college) {
            is College.Rutgers, College.FSU -> value.uppercase()
            is College.USF -> value
        }

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
                        state.school.selected
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
        val newSchool = globalData.schoolsByCode[school] ?: activeSchoolsByCode.values.first()
        if (college.showFullSchoolList)
            campusVM.selectOnly(Campus.valueOf(newSchool.code.uppercase()))
        val newDept = dept.takeIf { it in newSchool.depts } ?: newSchool.depts.first()
        selectDept(dept = newDept, school = newSchool, course = course, prof = prof)
    }

    fun selectDept(
        dept: String,
        school: School = activeSchoolsByCode[state.school.selected]
            ?: error("Selected School (${state.school.selected}) Not Found"),
        course: String? = null,
        prof: String? = null,
    ) {
        val profsChanged = state.prof.list.size != (statsByProf.size + 1) // (+1 for None)
        if (!isSameAsState(school.code, dept, course, prof) && !profsChanged) {
            // needed if campus is changed but currently selected school is still valid
            if (activeSchoolsByCode.keys != state.school.list)
                state = state.copy(school = state.school.copy(list = schoolList))
            return
        }
        pageLoading = true
        coroutineScope.launch {
            // prevent unnecessary network requests & calculations
            val newDept = state.school.selected != school.code || state.dept.selected != dept
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
            else state.prof.list

            val courses = if (newDept || profsChanged) // if profs changed, courses may have changed too
                statsByProf.values.flatMap { it.courseStats.keys }
                    .plus(deptData.teachingMap.keys.filter { it[0].isDigit() }) // add only courses from teachingMap
                    .toSet().sorted().prepend(None)
            else state.course.list

            val courseValid = course != None && course in courses
            val oldState = state
            state = state.copy(
                school = state.school.copy(schoolList, school.code),
                dept = state.dept.copy(school.associateDeptsToName(), dept),
                course = state.course.copy(courses, course.takeIf { courseValid } ?: None),
                prof = state.prof.copy(profs, prof.takeIf { !courseValid && it in profs } ?: None),
            )

            initialLoading = false
            if (oldState == state) pageLoading = false // must be done manually since screen won't recompose
        }
    }

    fun selectCourse(course: String) {
        val courseCode = with(course) {
            if (endsWith(")")) this.substringAfterBefore("(", ")") else this
        }
        state = state.copy(
            course = state.course.copy(selected = courseCode),
            prof = state.prof.copy(selected = None)
        )
    }

    fun selectProf(prof: String) {
        state = state.copy(
            prof = state.prof.copy(selected = prof),
            course = state.course.copy(selected = None)
        )
    }

    val profSummaryVM: ProfSummaryVM? by derivedStateOf {
        // 2nd condition perhaps not needed anymore probably
        if (status != Status.Prof || state.prof.selected !in statsByProf.keys) null
        else {
            ProfSummaryVM(
                statsByProf = statsByProf,
                selectedProf = state.prof.selected,
                deptUrl = getUrl(prof = None),
                teachingCourses = deptData.teachingMap[state.prof.selected] ?: emptyList(),
                defaultQuestion = college.questions.defaultIndex,
                getText = { getCode(course = it ?: None) },
                getCourseUrl = { getUrl(prof = None, course = it) },
                getGraphLabel = college.questions.getGraphLabel,
                goToDeptData = { selectProf(None) },
                goToCourseData = { selectCourse(it) }
            )
        }
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
                prof = urlParams["prof"]?.decodeURL()?.uppercase(),
            )
        }
    }

    fun onPopState(paramsUrl: String) {
        val params = paramsUrl.drop(1).split('&').associate {
            it.split('=', limit = 2).zipWithNext().getOrNull(0) ?: return
        }
        val school = params["school"] ?: return
        val dept = params["dept"] ?: return
        val course = params["course"]
        val prof = params["prof"]?.decodeURL()

        enableSchoolIfDisabled(school)

        // need to figure out how to deal wih invalid course/prof on pop -maybe?
        if (school != state.school.selected) {
            selectSchool(school = school, dept = dept, course = course, prof = prof)
        } else if (dept != state.dept.selected) {
            selectDept(dept = dept, course = course, prof = prof)
        } else if (course != null) {
            selectCourse(course)
        } else if (prof != null) {
            selectProf(prof)
        } else if (state.course.selected != None) {
            selectCourse(None)
        } else if (state.prof.selected != None) {
            selectProf(None)
        }
    }

    private fun School.associateDeptsToName(includeCode: Boolean = true): List<Pair<String, String>> {
        return depts.associateWith { dept ->
            globalData.deptMap[dept]?.let {
                if (includeCode) "$dept - $it" else it
                // considered using a "dept - " prefix but tht requires a monospace font to look good
            } ?: error("Invalid Dept ($dept)")
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
    return school != state.school.selected ||
            dept != state.dept.selected ||
            (course ?: None) != state.course.selected ||
            (prof ?: None) != state.prof.selected
}

private fun DataPageVM.getCode(
    school: String = state.school.selected,
    dept: String = state.dept.selected,
    course: String = state.course.selected,
): String = college.getCode(school, dept, course.let { if (it.isBlankOrNone()) "" else it })

private fun DataPageVM.getUrl(
    school: String = state.school.selected,
    dept: String = state.dept.selected,
    course: String = state.course.selected,
    prof: String = state.prof.selected,
): String {
    return "?" +
            "school=$school" +
            "&dept=$dept" +
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

enum class Status {
    InitialLoading, Dept, Course, Prof,
}

data class DropDownState<T>(
    val list: Collection<T> = emptyList(),
    val selected: String = "",
)

data class DataPageState(
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