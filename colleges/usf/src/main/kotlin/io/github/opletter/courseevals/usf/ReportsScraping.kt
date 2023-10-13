package io.github.opletter.courseevals.usf

import io.github.opletter.courseevals.common.data.pmap
import io.github.opletter.courseevals.common.remote.makeFileAndDir
import io.github.opletter.courseevals.common.remote.writeAsJson

suspend fun getData(writeDir: String, terms: List<String> = getTerms(), prefixes: List<String> = Prefixes) {
    prefixes.forEach { prefix ->
        terms.sortedDescending().chunked(terms.size / 2 + 1).forEach { chunk ->
            chunk.pmap { year ->
                println("$prefix $year")
                try {
                    val reportId = getReportIdByPrefix(prefix, year)
                    val data = getReports(reportId)
                    makeFileAndDir("$writeDir/$prefix/$year.json").writeAsJson(data)
                } catch (e: Exception) {
                    e.printStackTrace()
                    makeFileAndDir("$writeDir/errors.txt").appendText("$prefix $year ${e.message}\n")
                }
            }
        }
    }
}