package com.ramcosta.composedestinations.codegen.commons

import java.io.OutputStream
import java.util.Locale

operator fun OutputStream.plusAssign(str: String) {
    write(str.toByteArray())
}

operator fun StringBuilder.plusAssign(str: String) {
    append(str)
}

fun String.removeFromTo(from: String, to: String): String {
    val startIndex = indexOf(from)
    val endIndex = indexOf(to) + to.length

    return kotlin.runCatching { removeRange(startIndex, endIndex) }.getOrNull() ?: this
}

fun String.removeInstancesOf(vararg toRemove: String): String {
    var result = this
    toRemove.forEach {
        result = result.replace(it, "")
    }
    return result
}

private val keywords: Set<String> = setOf(
    "in", "is"
)

fun String.sanitizePackageName(): String {
    return split(".")
        .joinToString(".") { if (keywords.contains(it)) "`$it`" else it }
}

private val humps = "(?<=.)(?=\\p{Upper})".toRegex()
fun String.toSnakeCase() = replace(humps, "_").lowercase(Locale.US)

private val pattern = "_[a-z]".toRegex()
fun String.snakeToCamelCase(): String {
    return replace(pattern) { it.value.last().uppercase() }
}