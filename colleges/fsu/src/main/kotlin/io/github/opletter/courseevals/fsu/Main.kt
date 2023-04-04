package io.github.opletter.courseevals.fsu

suspend fun main(args: Array<String>) {
    if ("-teaching" in args)
        getTeachingProfs("jsonData/extraData/teachingF23")
//    getStatsByProf()
//    createAllInstructors()
//    getCompleteCourseNames()
//    getTeachingProfs("jsonData/extraData/teachingF23")
}