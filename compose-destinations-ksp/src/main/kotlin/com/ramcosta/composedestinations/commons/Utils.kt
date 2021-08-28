package com.ramcosta.composedestinations.commons

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import java.io.*

internal fun KSFunctionDeclaration.findAnnotation(name: String): KSAnnotation {
    return annotations.find { it.shortName.asString() == name }!!
}

internal inline fun <reified T> KSAnnotation.findArgumentValue(name: String): T? {
    return arguments.find { it.name?.asString() == name }?.value as T?
}

internal fun File.readLine(lineNumber: Int): String {
    val bufferedReader = BufferedReader(InputStreamReader(FileInputStream(this), Charsets.UTF_8))
    return bufferedReader
        .useLines { lines: Sequence<String> ->
            lines
                .take(lineNumber)
                .last()
        }
}