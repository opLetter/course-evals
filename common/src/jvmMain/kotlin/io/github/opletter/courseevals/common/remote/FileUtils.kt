package io.github.opletter.courseevals.common.remote

import java.io.File

fun makeFileAndDir(filename: String): File = File(filename).apply { parentFile.mkdirs() }