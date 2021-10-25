package com.ramcosta.composedestinations.codegen.commons

import com.ramcosta.composedestinations.codegen.model.Type
import java.io.OutputStream

operator fun OutputStream.plusAssign(str: String) {
    write(str.toByteArray())
}

operator fun StringBuilder.plusAssign(str: String) {
    append(str)
}

fun Type.toNavTypeCodeOrNull(): String? {
    return when (qualifiedName) {
        String::class.qualifiedName -> "NavType.StringType"
        Int::class.qualifiedName -> "NavType.IntType"
        Float::class.qualifiedName -> "NavType.FloatType"
        Long::class.qualifiedName -> "NavType.LongType"
        Boolean::class.qualifiedName -> "NavType.BoolType"
        else -> null
    }
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