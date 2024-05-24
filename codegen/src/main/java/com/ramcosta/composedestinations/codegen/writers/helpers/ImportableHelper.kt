package com.ramcosta.composedestinations.codegen.writers.helpers

import com.ramcosta.composedestinations.codegen.commons.plusAssign
import com.ramcosta.composedestinations.codegen.commons.sanitizePackageName
import com.ramcosta.composedestinations.codegen.model.Importable

class ImportableHelper(
    initialImports: Set<Importable> = emptySet()
) {

    private val imports = initialImports.toMutableSet()
    private val priorityImports = initialImports.toMutableSet()

    fun addPriorityQualifiedImport(qualifiedName: String, simpleName: String? = null) {
        val element = Importable(
            simpleName ?: qualifiedName.split(".").last(),
            qualifiedName
        )
        imports.add(element)
        priorityImports.add(element)
    }

    fun addPriorityQualifiedImport(importable: Importable) {
        imports.add(importable)
        priorityImports.add(importable)
    }

    fun remove(importables: Set<Importable>) {
        imports.removeAll(importables)
        priorityImports.removeAll(importables)
    }

    fun addAndGetPlaceholder(importable: Importable): String {
        imports.add(importable)
        return importable.qualifiedName
    }

    fun addAll(importables: Set<Importable>){
        imports.addAll(importables)
    }

    fun add(importable: Importable){
        imports.add(importable)
    }

    fun addResolvedImportsToSrcCode(currentFile: String): String {
        var final = currentFile

        val importableImportsBySimpleName: Map<String, List<Importable>> =
            imports.groupBy { it.preferredSimpleName }
        importableImportsBySimpleName.forEach {
            if (it.value.size > 1) {
                val importsToRemove =
                    it.value.filterTo(mutableSetOf()) { import -> import !in priorityImports }

                // removed imports won't be replaced by the simple name
                // so we also want to sanitize their qualified name
                importsToRemove.forEach { importable ->
                    final = final.replace(importable.qualifiedName, importable.qualifiedName.sanitizePackageName())
                }

                imports.removeAll(importsToRemove)
            }
        }

        imports.forEach {
            final = final.replace(it.qualifiedName, it.preferredSimpleName)
        }

        return "${additionalImports()}\n\n$final"
    }

    private fun additionalImports(): String {
        val importsStr = StringBuilder()

        imports
            .filter { it.qualifiedName != "kotlin.${it.simpleName}" }
            .map { it.importStatement }
            .toSet()
            .sorted()
            .forEach {
                importsStr += "\n$it"
            }

        return importsStr.toString()
    }
}