package com.ramcosta.composedestinations.ksp.commons

import com.google.devtools.ksp.findActualType
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeAlias
import com.ramcosta.composedestinations.codegen.model.Importable
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader


val ignoreAnnotations = listOf(
    "Composable",
    "Target",
    "Retention",
    "MustBeDocumented",
    "OptIn",
    "RequiresOptIn"
)

fun KSAnnotated.findAnnotation(name: String): KSAnnotation {
    return annotations.find { it.shortName.asString() == name }!!
}

fun KSAnnotated.findAnnotationPathRecursively(name: String, path: List<KSAnnotation> = emptyList()): List<KSAnnotation>? {
    val relevantAnnotations = annotations.filter { it.shortName.asString() !in ignoreAnnotations}
    val foundAnnotation = relevantAnnotations.find { it.shortName.asString() == name }
    if (foundAnnotation != null) {
        return path + foundAnnotation
    }

    relevantAnnotations.forEach { annotation ->
        val found = annotation.annotationType.resolve().declaration.findAnnotationPathRecursively(name, path + annotation)
        if (found != null) {
            return found
        }
    }

    return null
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

fun File.readLines(startLineNumber: Int, endLineNumber: Int): List<String> {
    val bufferedReader = BufferedReader(InputStreamReader(FileInputStream(this), Charsets.UTF_8))
    return bufferedReader
        .useLines { lines: Sequence<String> ->
            val linesList = lines
                .take(endLineNumber)
                .toList()
            linesList
                .takeLast(linesList.size - (startLineNumber - 1))
        }
}

fun File.readLinesAndImports(startLineNumber: Int, endLineNumber: Int): Pair<List<String>, List<String>> {
    val bufferedReader = BufferedReader(InputStreamReader(FileInputStream(this), Charsets.UTF_8))
    return bufferedReader
        .useLines { lines: Sequence<String> ->
            val linesList = lines
                .take(endLineNumber)
                .toList()

            val linesRes = linesList.takeLast(linesList.size - (startLineNumber - 1))
            val imports = linesList.filter { it.startsWith("import") }
                .map { it.removePrefix("import ") }

            linesRes to imports
        }
}

fun KSAnnotated.findAllRequireOptInAnnotations(): List<Importable> {
    val requireOptInAnnotations = mutableListOf<Importable>()
    annotations.forEach { annotation ->
        val annotationShortName = annotation.shortName.asString()
        if (annotationShortName == "Composable" || annotationShortName == "Destination") {
            return@forEach
        }

        val ksType = annotation.annotationType.resolve()
        if (ksType.isRequireOptInAnnotation()) {
            requireOptInAnnotations.add(Importable(annotationShortName, ksType.declaration.qualifiedName!!.asString()))
        }
    }

    return requireOptInAnnotations
}

fun KSType.isRequireOptInAnnotation(): Boolean {
    return declaration.annotations.any { annotation ->
        annotation.shortName.asString() == "RequiresOptIn"
                || annotation.annotationType.annotations.any {
            annotation.annotationType.resolve().isRequireOptInAnnotation()
        }
    }
}

fun KSType.findActualClassDeclaration(): KSClassDeclaration? {
    if (this.declaration is KSTypeAlias) {
        return (this.declaration as KSTypeAlias).findActualType()
    }

    return declaration as? KSClassDeclaration?
}

fun KSClassDeclaration.toImportable(): Importable? {
    return qualifiedName?.let { nonNullQualifiedName ->
        Importable(
            simpleName.asString(),
            nonNullQualifiedName.asString()
        )
    }
}

val KSClassDeclaration.isNothing get() =
    qualifiedName?.asString() == "java.lang.Void" || qualifiedName?.asString() == "kotlin.Nothing"
