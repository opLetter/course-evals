package io.github.opletter.courseevals.common.data

typealias SchoolDeptsMap<T> = Map<String, Map<String, T>>

inline fun <T> SchoolDeptsMap<T>.forEachDept(action: (String, String, T) -> Unit) {
    forEach { (school, deptMap) ->
        deptMap.forEach { (dept, something) ->
            action(school, dept, something)
        }
    }
}

inline fun <T, R> SchoolDeptsMap<T>.mapEachDept(transform: (String, String, T) -> R): SchoolDeptsMap<R> {
    return mapValues { (school, deptMap) ->
        deptMap.mapValues { (dept, something) ->
            transform(school, dept, something)
        }
    }
}

inline fun <T, R> SchoolDeptsMap<T>.flatMapEachDept(transform: (String, String, T) -> List<R>): List<R> {
    return flatMap { (school, deptMap) ->
        deptMap.flatMap { (dept, something) ->
            transform(school, dept, something)
        }
    }
}

fun <T, R> SchoolDeptsMap<Map<T, R>>.filterNotEmpty(): SchoolDeptsMap<Map<T, R>> {
    return mapValues { (_, v) ->
        v.filterValues { it.isNotEmpty() }
    }.filterValues { it.isNotEmpty() }
}