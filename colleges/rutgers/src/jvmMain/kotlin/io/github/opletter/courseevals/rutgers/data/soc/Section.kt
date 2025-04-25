package io.github.opletter.courseevals.rutgers.data.soc

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNull

@Serializable
data class Section(
    val campusCode: String,
    val comments: List<DescriptionHolder>,
    val commentsText: String,
    val courseFee: String? = null,
    val crossListedSectionType: String,
    val crossListedSections: List<CrossListedSections>,
    val crossListedSectionsText: String,
    val examCode: String,
    val examCodeText: String,
    val finalExam: JsonNull, // Currently always null
    val honorPrograms: List<Code>,
    val index: String,
    val instructors: List<SOCInstructor>,
    val instructorsText: String,
    val legendKey: String?,
    val majors: List<Major>,
    val meetingTimes: List<MeetingTime>,
    val minors: List<Code>,
    val number: String,
    val openStatus: Boolean,
    val openStatusText: String,
    val openToText: String,
    val printed: String,
    val sectionCampusLocations: List<DescriptionHolder>,
    val sectionCourseType: String,
    val sectionEligibility: String?,
    val sectionNotes: String,
    val sessionDatePrintIndicator: String,
    val sessionDates: String?,
    val specialPermissionAddCode: String?,
    val specialPermissionAddCodeDescription: String?,
    val specialPermissionDropCode: String?,
    val specialPermissionDropCodeDescription: String?,
    val subtitle: String,
    val subtopic: String,
    val unitMajors: List<UnitMajor>,
)