package io.github.opletter.courseevals.site.core.misc

import io.github.opletter.courseevals.common.data.Campus
import io.github.opletter.courseevals.common.data.InstructorStats
import io.github.opletter.courseevals.common.data.Semester
import io.github.opletter.courseevals.common.data.SemesterType
import io.github.opletter.courseevals.common.remote.GithubSource
import io.github.opletter.courseevals.common.remote.WebsiteDataSource
import io.github.opletter.courseevals.common.remote.WebsitePaths
import io.github.opletter.courseevals.site.core.states.Questions

sealed interface College {
    val fullName: String
    val urlPath: String
    val questions: Questions
    val searchHint: String

    /** How to label the dept/course across the UI. [course] is empty if no course is selected. */
    fun getCode(school: String, dept: String, course: String): String

    /** Modify the input string of the search bar */
    fun searchInputTransform(value: String): String

    /** Labels for the nav dropdowns, in order of appearance. Length: 4 */
    val dropDownLabels: List<String>

    // Unsure about what to choose for this default value.
    // Ideally it'd be as recent as possible (for page loading speed), but not too recent (for relevance)
    // Chosen for now to be the 5th semester back (from which we have data)
    // Considered making it the first semester of current-year seniors, but that may slow down pages too much
    // for data that most people wouldn't want to see.
    val semesterConfig: SemesterConfig<*>

    /** All valid campuses for this college, and whether they should be enabled by default */
    val campuses: Map<Campus, Boolean>
    val schoolStrategy: SchoolStrategy
    val options: Set<ExtraOptions>
    val dataSource: WebsiteDataSource

    class Rutgers(override val dataSource: WebsiteDataSource) : College {
        override val fullName = "Rutgers University"
        override val urlPath = "rutgers"

        private val tenQs = listOf(
            "The instructor was prepared for class and presented the material in an organized manner",
            "The instructor responded effectively to student comments and questions",
            "The instructor generated interest in the course material",
            "The instructor had a positive attitude toward assisting all students in understanding course material",
            "The instructor assigned grades fairly",
            "The instructional methods encouraged student learning",
            "I learned a great deal in this course",
            "I had a strong prior interest in the subject matter and wanted to take this course",
            "I rate the teaching effectiveness of the instructor as",
            "I rate the overall quality of the course as",
        )

        private val tenQsShortened = listOf(
            "Prepared & Organized",
            "Responded to Questions Effectively",
            "Generated Interest in Material",
            "Good Attitude Towards Assisting Students",
            "Graded Fairly",
            "Effective Teaching Methods",
            "Learned a Great Deal",
            "Strong Prior Interest",
            "Teaching Effectiveness",
            "Overall Quality of Course",
        )
        private val usefulQuestions = tenQs.minus(tenQs[7])
        private val usefulQuestionsShort = tenQsShortened.minus(tenQsShortened[7])
        override val questions = Questions(usefulQuestions, usefulQuestionsShort, 7) {
            if (it < 7) "Disagree -> Agree" else "Poor -> Excellent"
        }
        override val searchHint = "'SMITH', '01:198:112', 'MATH', ..."
        override fun getCode(school: String, dept: String, course: String): String =
            "$school:$dept" + if (course.isEmpty()) "" else ":$course"

        override fun searchInputTransform(value: String): String = value.uppercase()
        override val dropDownLabels = listOf("School", "Subject", "Course (Optional)", "Instructor (Optional)")

        override val semesterConfig = SemesterConfig(
            bounds = Semester.Double.valueOf(SemesterType.Spring, 2014) to
                    Semester.Double.valueOf(SemesterType.Spring, 2022),
            default = Semester.Double.valueOf(SemesterType.Spring, 2020),
        ) { Semester.Double.valueOf(it) }
        override val campuses = mapOf(Campus.NB to true, Campus.CM to true, Campus.NK to true)
        override val schoolStrategy = SchoolStrategy.NORMAL
        override val options = setOf(ExtraOptions.CAMPUS, ExtraOptions.MIN_SEM)

        companion object {
            private fun privateSource(token: String): WebsiteDataSource {
                val privateSource = GithubSource(
                    paths = WebsitePaths("rutgers/processed"), // TODO: use "generated"
                    repoPath = "DennisTsar/Rutgers-Evals-Data",
                    token = token
                )
                return object : WebsiteDataSource by publicSource {
                    override suspend fun getStatsByProf(school: String, dept: String): Map<String, InstructorStats> =
                        privateSource.getStatsByProf(school, dept)
                }
            }

            private val publicSource = GithubSource(
                paths = WebsitePaths(
                    baseDir = "rutgers/generated",
                    statsByProfDir = "rutgers/generated/stats-by-prof-cleaned",
                )
            )

            val Public = Rutgers(publicSource)

            @Suppress("FunctionName") // purposely mimic a constructor
            fun Private(token: String) = Rutgers(privateSource(token))
        }
    }

    object FSU : College {
        override val fullName = "Florida State University"
        override val urlPath = "fsu"

        private val questionsLong = listOf(
//            "The course materials helped me understand the subject matter.",
            "The work required of me was appropriate based on course objectives.",
            "The tests, project, etc. accurately measured what I learned in this course.",
//            "This course encouraged me to think critically.",
//            "I learned a great deal in this course.",
            "Instructor(s) provided clear expectations for the course.",
            "Instructor(s) communicated effectively.",
            "Instructor(s) stimulated my interest in the subject matter.",
            "Instructor(s) provided helpful feedback on my work.",
            "Instructor(s) demonstrated respect for students.",
            "Instructor(s) demonstrated mastery of the subject matter.",
//            "Overall course content rating.",
            "Overall rating for Instructor(s)"
        )
        private val questionsShort = listOf(
//            "Helpful course materials",
            "Appropriate amount of work",
            "Accurate assessment of learning",
//            "Encouraged critical thinking",
//            "Learned a great deal",
            "Instructor provided clear expectations",
            "Instructor communicated well", // ?
            "Instructor stimulated interest",
            "Instructor provided helpful feedback",
            "Instructor demonstrated respect",
            "Instructor showed mastery of subject",
//            "Overall course content rating",
            "Overall rating for Instructor",
        )
        override val questions = Questions(questionsLong, questionsShort, 8) {
            if (it < 8) "Disagree -> Agree" else "Poor -> Excellent"
        }
        override val searchHint = "'SMITH', 'COP3330', 'MATH', ..."

        // ignore the school, since it's a campus and only one campus is selected at a time
        override fun getCode(school: String, dept: String, course: String): String = dept + course

        override fun searchInputTransform(value: String): String = value.uppercase()

        override val dropDownLabels = listOf("Campus", "Course Prefix", "Course (Optional)", "Instructor (Optional)")
        override val semesterConfig = SemesterConfig(
            bounds = Semester.Triple.valueOf(SemesterType.Fall, 2013) to
                    Semester.Triple.valueOf(SemesterType.Fall, 2024),
            default = Semester.Triple.valueOf(SemesterType.Fall, 2021),
        ) { Semester.Triple.valueOf(it) }
        override val campuses = mapOf(Campus.MAIN to true, Campus.PNM to false, Campus.INTL to false)
        override val schoolStrategy = SchoolStrategy.SHOW_ALL
        override val options = setOf(ExtraOptions.MIN_SEM)

        override val dataSource = GithubSource(paths = WebsitePaths(baseDir = "fsu/generated"))
    }

    object USF : College {
        override val fullName = "University of South Florida"
        override val urlPath = "usf"

        private val questionsLong = listOf(
            "Description of Course Objectives & Assignments",
            "Communication of Ideas and Information",
            "Expression of Expectations for Performance",
            "Availability to Assist Students In or Out of Class",
            "Respect and Concern for the Students",
            "Stimulation of Interest in the Course",
            "Facilitation of Learning",
            "Overall Rating of the Instructor",
        )
        private val questionsShort = listOf(
            "Description of Course",
            "Communication of Ideas & Info",
            "Expression of Expectations",
            "Availability to Assist",
            "Respect & Concern For Students",
            "Stimulation of Interest",
            "Facilitation of Learning",
            "Overall Rating of Instructor",
        )
        override val questions = Questions(questionsLong, questionsShort, 7) { "Poor -> Excellent" }
        override val searchHint = "'Smith', 'COP3330', 'PHY', ..."

        // ignore the school since there's only one
        override fun getCode(school: String, dept: String, course: String): String = dept + course

        override fun searchInputTransform(value: String): String = value

        override val dropDownLabels = listOf("N/A", "Subject", "Course (Optional)", "Instructor (Optional)")
        override val semesterConfig = SemesterConfig(
            bounds = Semester.Triple.valueOf(SemesterType.Fall, 2013) to
                    Semester.Triple.valueOf(SemesterType.Fall, 2024),
            default = Semester.Triple.valueOf(SemesterType.Fall, 2021),
        ) { Semester.Triple.valueOf(it) }
        override val campuses = mapOf(Campus.MAIN to true)
        override val schoolStrategy = SchoolStrategy.SINGLE
        override val options = setOf(ExtraOptions.MIN_SEM)

        override val dataSource = GithubSource(paths = WebsitePaths(baseDir = "usf/generated"))
    }

    object TXST : College {
        override val fullName = "Texas State University"
        override val urlPath = "txst"

        private val questionsLong = listOf(
            "The instructor provided opportunity to learn",
            "The instructor communicated effectively",
            "The instructor conducted class as scheduled",
            "The course goals were made clear",
            "The course was organized effectively",
        )
        private val questionsShort = listOf(
            "Provided Opportunity to Learn",
            "Communicated Effectively",
            "Held Class as Scheduled",
            "Gave Clear Goals",
            "Organized Effectively",
        )
        override val questions = Questions(questionsLong, questionsShort, 0) { "Disagree -> Agree" }
        override val searchHint = "'Smith', 'CS3338', 'MATH', ..."

        // ignore the school since there's only one
        override fun getCode(school: String, dept: String, course: String): String = dept + course

        override fun searchInputTransform(value: String): String = value

        override val dropDownLabels = listOf("N/A", "Course Prefix", "Course (Optional)", "Instructor (Optional)")
        override val semesterConfig = SemesterConfig(
            bounds = Semester.Triple.valueOf(SemesterType.Fall, 2021) to
                    Semester.Triple.valueOf(SemesterType.Spring, 2024),
            default = Semester.Triple.valueOf(SemesterType.Fall, 2021),
        ) { Semester.Triple.valueOf(it) }
        override val campuses = mapOf(Campus.MAIN to true)
        override val schoolStrategy = SchoolStrategy.SINGLE
        override val options = setOf(ExtraOptions.MIN_SEM)

        override val dataSource = GithubSource(paths = WebsitePaths(baseDir = "txst/generated"))
    }
}

enum class ExtraOptions {
    CAMPUS, MIN_SEM
}

enum class SchoolStrategy {
    NORMAL, SHOW_ALL, SINGLE
}

class SemesterConfig<T : Semester<T>>(
    val bounds: Pair<T, T>,
    val default: T,
    val builder: (Int) -> T,
)