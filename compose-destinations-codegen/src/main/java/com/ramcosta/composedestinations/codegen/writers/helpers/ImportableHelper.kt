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
            imports.groupBy { it.simpleName }
        importableImportsBySimpleName.forEach {
            if (it.value.size > 1) {
                imports.removeAll(it.value.filterTo(mutableSetOf()) { import -> import !in priorityImports })
            }
        }

        imports.forEach {
            final = final.replace(it.qualifiedName, it.simpleName)
        }

        return "${additionalImports()}\n\n$final"
    }

    private fun additionalImports(): String {
        val importsStr = StringBuilder()

        imports
            .filter { it.qualifiedName != "kotlin.${it.simpleName}" }
            .map { it.qualifiedName }
            .sorted()
            .forEach {
                importsStr += "\nimport ${it.sanitizePackageName()}"
            }

        return importsStr.toString()
    }
}