package io.github.opletter.courseevals.rutgers.misc

import io.github.opletter.courseevals.common.data.SchoolDeptsMap
import io.github.opletter.courseevals.common.data.forEachDept

fun SchoolDeptsMap<Map<String, *>>.printPossibleNameAdjustments(printURL: Boolean = true) {
    forEachDept { school, dept, profMap ->
        val names = profMap.keys.sorted()
        val pairs = names.mapIndexed { index, s ->
            if (s.substringBefore(",").length == 1)
                println("swapped? ($school-$dept): $s")
            names.drop(index + 1).map { Pair(s, it) }
        }.flatten()

        pairs.filter { (first, second) ->
            val a = first.split(" ", ",").filter { it.isNotBlank() }
            val b = second.split(" ", ",").filter { it.isNotBlank() }
            similarity(a[0], b[0]) > 0.75
                    || (similarity(a[0], b[0]) > 0.33 && similarity(a.getOrNull(1), b.getOrNull(1)) > 0.75)
                    // checks for flipped first/last names
                    || (similarity(a[0], b.getOrNull(1)) > 0.75 && similarity(a.getOrNull(1), b[0]) > 0.75)
        }.ifEmpty { null }?.let { filtered ->
            println("\n\"$school:$dept\" -> when (prof) {")
            filtered.sortedBy { it.second }.forEach { (a, b) ->

//                fun List<Entry>.printExtra() {
//                    with(map { it.semester }) { println("${min()} -> ${max()}") }
//                    println(map { it.course }.toSet().sorted())
//                }
//                profMap[a]?.printExtra()
//                profMap[b]?.printExtra()

//                println("***************")
//                (profMap[a]!!+profMap[b]!!)
//                    .sortedWith(compareBy({ it.semester }, { it.course }))
//                    .forEach { println("\t${it.formatFullName()} ${it.semester} ${it.course}") }
//                println("***************")

                print("\t\"$b\" -> \"$a\"")
                val common = b.zip(a).takeWhile { (x, y) -> x == y }
                    .map { it.first }.joinToString("").split(",")[0]
                    .ifBlank { // go based on first names if last names different
                        b.substringAfter(", ").zip(a.substringAfter(", "))
                            .takeWhile { (x, y) -> x == y }
                            .map { it.first }.joinToString("")
                    }
                if (printURL)
                    println(
                        " https://sirs.ctaar.rutgers.edu/index.php?mode=name&survey%5Blastname%5D=$common" +
                                "&survey%5Bsemester%5D=&survey%5Byear%5D=&survey%5Bschool%5D=&survey%5Bdept%5D=$dept)"
                    )
                else println()
            }
            println("\telse -> prof\n}")
        }
    }
}