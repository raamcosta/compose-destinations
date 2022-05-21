package com.ramcosta.composedestinations.codegen.writers.helpers

import com.ramcosta.composedestinations.codegen.commons.plusAssign
import com.ramcosta.composedestinations.codegen.commons.sanitizePackageName
import com.ramcosta.composedestinations.codegen.model.DestinationGeneratingParamsWithNavArgs
import com.ramcosta.composedestinations.codegen.model.Importable

class ImportableHelper(
    destination: DestinationGeneratingParamsWithNavArgs,
) {

    private val imports = mutableSetOf<Importable>()
    private val priorityImports = mutableSetOf<Importable>()

    init {
        addPriorityQualifiedImport(destination.composableQualifiedName, destination.composableName)
    }

    fun addPriorityQualifiedImport(qualifiedName: String, simpleName: String? = null) {
        val element = Importable(
            simpleName ?: qualifiedName.split(".").last(),
            qualifiedName
        )
        imports.add(element)
        priorityImports.add(element)
    }

    fun addImportableAndGetPlaceholder(importable: Importable): String {
        imports.add(importable)
        return importable.qualifiedName
    }

    fun resolveImportablePlaceHolders(importsPlaceHolder: String, currentFile: String): String {
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

        return final.replace(importsPlaceHolder, additionalImports())
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