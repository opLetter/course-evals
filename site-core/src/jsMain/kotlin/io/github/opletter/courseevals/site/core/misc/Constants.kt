package io.github.opletter.courseevals.site.core.misc

import io.github.opletter.courseevals.common.data.Semester
import io.github.opletter.courseevals.common.data.SemesterType

const val None = "None" // to avoid accidental typos

const val keyReset = 40 // seems to be roughly the performance sweet-spot

val TeachingSem = Semester.Double.valueOf(SemesterType.Fall, 2026)