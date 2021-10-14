package com.ramcosta.composedestinations.codegen.commons

import java.io.OutputStream

operator fun OutputStream.plusAssign(str: String) {
    write(str.toByteArray())
}

operator fun StringBuilder.plusAssign(str: String) {
    append(str)
}

fun String.removeFromTo(from: String, to: String): String {
    val startIndex = indexOf(from)
    val endIndex = indexOf(to) + to.length

    return removeRange(startIndex, endIndex)
}

fun String.removeInstancesOf(toRemove: String): String {
    return replace(toRemove, "")
}

fun String.replaceEach(toReplace: String, replaceWith: (index: Int, currentString: String) -> String): String {
    var toReturn = this

    while (true) {
        val indexToChange = toReturn.indexOf(toReplace)
        if (indexToChange == -1) break

        toReturn = toReturn.replaceRange(indexToChange, indexToChange + toReplace.length, replaceWith(indexToChange, toReturn))
    }

    return toReturn
}