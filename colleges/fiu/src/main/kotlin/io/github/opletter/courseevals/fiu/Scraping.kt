package io.github.opletter.courseevals.fiu

import io.github.opletter.courseevals.common.writeAsJson
import io.github.opletter.tableau.TableauScraper
import io.github.opletter.tableau.TableauWorksheet
import io.ktor.client.plugins.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.retry
import kotlinx.datetime.Clock
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.jetbrains.kotlinx.dataframe.api.rows
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.createParentDirectories
import kotlin.io.path.readLines
import kotlin.io.path.writeText
import kotlin.time.Duration.Companion.minutes

// If the filter leaves too much data, it's rendered server-side, and we don't have access to it
// So initially we gather all the data we can by just filtering instructor name, recording errors [getAllData]
// Then, for the errors, we filter by year as well, and record errors again [getAllDataLevel2]
// Finally, we additionally filter by semester & course, and hopefully that's all the data we need [getAllDataLevel3]

// Note: as of this writing, no data is available for:
// fall 2016, spring 2017, summer 2017, fall 2017
// but the code still checks them, which can probably be skipped

suspend fun getAllData(writeDir: Path) {
    var scraper = createScraper().apply { loads(DASHBOARD_URL) }

    var startTime = Clock.System.now()

    var ws = scraper.getWorkbook().worksheets[0]
        .setFilter(INSTUCTOR_NAME, "", indexValues = listOf(0))
        .worksheets[0].clearFilter(ACAD_YR)
        .worksheets[0].clearFilter(SEMESTER).worksheets[0]

    (0..50000).chunked(50).forEach { indices ->
        // reload after 30 mins to avoid connection loss
        if (Clock.System.now() - startTime > 30.minutes) {
            scraper = createScraper()
            // abuse flow for retry functionality
            flow<Unit> { scraper.loads(DASHBOARD_URL) }.retry(3) {
                it.printStackTrace()
                println("retrying...")
                delay(1.minutes)
                true
            }.collect()

            ws = scraper.getWorkbook().worksheets[0]
                .setFilter(INSTUCTOR_NAME, "", indexValues = listOf(0))
                .worksheets[0].clearFilter(ACAD_YR)
                .worksheets[0].clearFilter(SEMESTER).worksheets[0]
            startTime = Clock.System.now()
        }
        // manual restart: if latest file is fiu(x).json then this number is (x+50)
        // if (indices.first() < 7900) return@forEach

        val data = indices.map { idx ->
            println(idx)
            try {
                ws.setFilter(INSTUCTOR_NAME, "", indexValues = listOf(idx))
                    .worksheets[0]
                    .getEntries()
            } catch (e: Exception) {
                writeDir.resolve("failure.txt")
                    .createParentDirectories()
                    .writeText("$idx\n", Charsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND)
                emptyList()
            }
        }
        writeDir.resolve("fiu${indices.first()}.json").writeAsJson(data)
    }
}

suspend fun getAllDataLevel2(writeDir: Path, failedIndicesFile: Path) {
    val scraper = createScraper().apply { loads(DASHBOARD_URL) }

    var ws = scraper.getWorkbook().worksheets[0]
        .setFilter(INSTUCTOR_NAME, "", indexValues = listOf(0))
        .worksheets[0].clearFilter(ACAD_YR)
        .worksheets[0].clearFilter(SEMESTER).worksheets[0]

    val failedProfIndices = failedIndicesFile.readLines().map { it.toInt() }
    val originalNameOrdering = scraper.getNameOrdering()
    val failedNames = failedProfIndices
        .associateWith { originalNameOrdering[it].jsonObject["label"]!!.jsonPrimitive.content }

    failedNames.forEach { (origIndex, name) ->
        try {
            val newData = YearRange.flatMap { yearIndex ->
                ws = ws.setFilter(ACAD_YR, "", indexValues = listOf(yearIndex)).worksheets[0]

                val curNameIndex = scraper.getNameOrdering()
                    .indexOfFirst { it.jsonObject["label"]!!.jsonPrimitive.content == name }
                if (curNameIndex == -1) {
                    println("couldn't find $name ($origIndex) for semester $yearIndex")
                    return@flatMap emptyList<Entry>()
                }

                ws = ws.setFilter(INSTUCTOR_NAME, "", indexValues = listOf(curNameIndex)).worksheets[0]
                ws.getEntries()
            }
            writeDir.resolve("$origIndex.json").writeAsJson(newData)
        } catch (e: Exception) {
            writeDir.resolve("failure.txt")
                .createParentDirectories()
                .writeText("$origIndex\n", Charsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND)
        }
    }
}

suspend fun getAllDataLevel3(name: String, index: Int, writeDir: Path) {
    val scraper = createScraper().apply { loads(DASHBOARD_URL) }
    val newData = (0..2).flatMap { semesterIndex ->
        val ws = scraper.getWorkbook().worksheets[0].apply {
            setFilter(INSTUCTOR_NAME, "", indexValues = listOf(0), filterDelta = true)
            setFilter(SEMESTER, "", indexValues = listOf(semesterIndex), filterDelta = true)
        }

        YearRange.flatMap year@{ yearIndex ->
            ws.apply {
                setFilter(INSTUCTOR_NAME, "", indexValues = listOf(0), filterDelta = true)
                setFilter(ACAD_YR, "", indexValues = listOf(yearIndex), filterDelta = true)
                setFilter(INSTUCTOR_NAME, "", indexValues = listOf(0), filterDelta = true)
            }

            val curNameIndex = scraper.getNameOrdering()
                .indexOfFirst { it.jsonObject["label"]!!.jsonPrimitive.content == name }

            if (curNameIndex == -1) {
                println("couldn't find $curNameIndex")
                return@year emptyList()
            }

            val instructorWorkbook = ws
                .setFilter(INSTUCTOR_NAME, "", indexValues = listOf(curNameIndex), filterDelta = true)

            val courseIndices = scraper.filters["SPOTs Results"]!!
                .first { it["column"]!!.jsonPrimitive.content == COURSE_FILTER }
                .jsonObject["values"]!!.jsonArray.indices

            // only filter by courses if there's more than one
            if (courseIndices.count() == 1) {
                instructorWorkbook.worksheets[0].getEntries()
            } else {
                courseIndices.flatMap { courseIndex ->
                    println("starting $courseIndex")
                    ws.setFilter(COURSE_FILTER, "", indexValues = listOf(courseIndex), filterDelta = true)
                        .worksheets[0]
                        .getEntries()
                }.also { ws.clearFilter(COURSE_FILTER) }
            }
        }
    }
    writeDir.resolve("$index.json").writeAsJson(newData)
}

private val YearRange = 0..11

private const val INSTUCTOR_NAME = "Instructor Name"
private const val ACAD_YR = "Acad Yr"
private const val SEMESTER = "Semester"
private const val COURSE_FILTER = "Course (filter)"

private const val DASHBOARD_URL =
    "https://analytics.fiu.edu/t/AIM/views/FacultyEvaluations_1/StudentAssessmentofInstruction"

private fun createScraper(): TableauScraper {
    return TableauScraper {
        install(HttpTimeout) {
            requestTimeoutMillis = 60_000
        }
    }
}

private fun TableauScraper.getNameOrdering(): JsonArray {
    return filters["SPOTs Results"]!!
        .first { it["column"]?.jsonPrimitive?.content == INSTUCTOR_NAME }["selectionAlt"]!!.jsonArray
        .first().jsonObject["domainTables"]!!.jsonArray
}

private fun TableauWorksheet.getEntries(): List<Entry> =
    data.rows().chunked(48).map { Entry.fromRows(it) }