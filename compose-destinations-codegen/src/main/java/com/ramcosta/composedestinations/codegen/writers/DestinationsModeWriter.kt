package com.ramcosta.composedestinations.codegen.writers

import com.ramcosta.composedestinations.codegen.codeGenBasePackageName
import com.ramcosta.composedestinations.codegen.commons.plusAssign
import com.ramcosta.composedestinations.codegen.commons.sourceIds
import com.ramcosta.composedestinations.codegen.facades.CodeOutputStreamMaker
import com.ramcosta.composedestinations.codegen.model.GeneratedDestination
import com.ramcosta.composedestinations.codegen.moduleName
import com.ramcosta.composedestinations.codegen.templates.*
import java.io.OutputStream
import java.util.*

class DestinationsModeWriter(
    private val codeGenerator: CodeOutputStreamMaker,
) {

    private val additionalImports = mutableSetOf<String>()

    fun write(generatedDestinations: List<GeneratedDestination>) {
        if (generatedDestinations.isEmpty() || generatedDestinations.size == 1) {
            return
        }

        val file: OutputStream = codeGenerator.makeFile(
            packageName = codeGenBasePackageName,
            name = "${moduleName}Destinations",
            sourceIds = sourceIds(generatedDestinations).toTypedArray()
        )

        file += moduleDestinationTemplate
            .replace(MODULE_DESTINATIONS_PLACEHOLDER, moduleDestinationsCode(generatedDestinations))
            .replace(MODULE_DESTINATIONS_LIST_NAME_PLACEHOLDER, listName())
            .replace(REQUIRE_OPT_IN_ANNOTATIONS_PLACEHOLDER, requireOptInAnnotations(generatedDestinations))
            .replace(ADDITIONAL_IMPORTS, additionalImports())

        file.close()
    }

    private fun listName(): String {
        return "${moduleName.replaceFirstChar { it.lowercase(Locale.US) }}${if (moduleName.isEmpty()) "d" else "D"}estinations"
    }

    private fun moduleDestinationsCode(generatedDestinations: List<GeneratedDestination>): String {
        val code = StringBuilder()
        generatedDestinations.forEachIndexed { idx, it ->
            code += "\t${it.simpleName}"

            if (idx != generatedDestinations.lastIndex) {
                code += ",\n"
            }
        }

        return code.toString()
    }

    private fun requireOptInAnnotations(generatedDestinations: List<GeneratedDestination>): String {
        val requireOptInClassTypes = generatedDestinations.flatMapTo(mutableSetOf()) { it.requireOptInAnnotationTypes }
        val code = StringBuilder()

        requireOptInClassTypes.forEach { annotationType ->
            additionalImports.add(annotationType.qualifiedName)
            code += "@${annotationType.simpleName}\n"
        }

        return code.toString()
    }

    private fun additionalImports(): String {
        val imports = StringBuilder()

        additionalImports.sorted().forEachIndexed { idx, it ->
            if (idx == 0) imports += "\n"

            imports += "import $it\n"
        }

        return imports.toString()
    }
}