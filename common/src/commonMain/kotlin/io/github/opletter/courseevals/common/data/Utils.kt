package io.github.opletter.courseevals.common.data

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

fun String.substringAfterBefore(after: String, before: String): String = substringAfter(after).substringBefore(before)

//Stolen from te interwebs
suspend inline fun <A, B> Iterable<A>.pmap(crossinline f: suspend (A) -> B): List<B> =
    coroutineScope { map { async { f(it) } }.awaitAll() }

suspend inline fun <A, B, C> Map<A, B>.pmap(crossinline f: suspend (Map.Entry<A, B>) -> C): List<C> =
    coroutineScope { map { async { f(it) } }.awaitAll() }

fun <T> Collection<T>.prepend(element: T): List<T> = listOf(element) + this