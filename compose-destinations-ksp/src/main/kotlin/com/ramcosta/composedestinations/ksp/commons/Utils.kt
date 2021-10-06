package com.ramcosta.composedestinations.ksp.commons

import com.google.devtools.ksp.symbol.*
import com.ramcosta.composedestinations.codegen.commons.*
import com.ramcosta.composedestinations.codegen.model.*
import java.io.*

fun KSAnnotated.findAnnotation(name: String): KSAnnotation {
    return annotations.find { it.shortName.asString() == name }!!
}

inline fun <reified T> KSAnnotation.findArgumentValue(name: String): T? {
    return arguments.find { it.name?.asString() == name }?.value as T?
}

fun File.readLine(lineNumber: Int): String {
    val bufferedReader = BufferedReader(InputStreamReader(FileInputStream(this), Charsets.UTF_8))
    return bufferedReader
        .useLines { lines: Sequence<String> ->
            lines
                .take(lineNumber)
                .last()
        }
}

fun KSFunctionDeclaration.findAllRequireOptInAnnotations(): List<String> {
    val requireOptInAnnotations = mutableListOf<String>()
    annotations.forEach { annotation ->
        val annotationShortName = annotation.shortName.asString()
        if (annotationShortName == "Composable" || annotationShortName == "Destination") {
            return@forEach
        }

        if (annotation.isRequireOptIn()) {
            requireOptInAnnotations.add(annotationShortName)
        }
    }

    return requireOptInAnnotations
}

fun KSAnnotation.isRequireOptIn(): Boolean {
    val annotations = annotationType.resolve().declaration.annotations
    return annotations.any { annotation ->
        val annotationSimpleName = annotation.annotationType.resolve().declaration.simpleName.asString()
        annotationSimpleName == "RequiresOptIn"
                || annotation.annotationType.annotations.any { it.isRequireOptIn() }
    }
}
