package com.ramcosta.composedestinations.codegen.writers.sub

import com.ramcosta.composedestinations.codegen.codeGenBasePackageName
import com.ramcosta.composedestinations.codegen.commons.plusAssign
import com.ramcosta.composedestinations.codegen.commons.sourceIds
import com.ramcosta.composedestinations.codegen.facades.CodeOutputStreamMaker
import com.ramcosta.composedestinations.codegen.model.GeneratedDestination
import com.ramcosta.composedestinations.codegen.moduleName
import com.ramcosta.composedestinations.codegen.templates.MODULE_DESTINATIONS_LIST_NAME_PLACEHOLDER
import com.ramcosta.composedestinations.codegen.templates.MODULE_DESTINATIONS_PLACEHOLDER
import com.ramcosta.composedestinations.codegen.templates.REQUIRE_OPT_IN_ANNOTATIONS_PLACEHOLDER
import com.ramcosta.composedestinations.codegen.templates.moduleDestinationTemplate
import com.ramcosta.composedestinations.codegen.writers.helpers.ImportableHelper
import com.ramcosta.composedestinations.codegen.writers.helpers.writeSourceFile
import java.util.*

class DestinationsModeWriter(
    private val codeGenerator: CodeOutputStreamMaker,
) {

    private val importableHelper = ImportableHelper(moduleDestinationTemplate.imports)

    fun write(generatedDestinations: List<GeneratedDestination>) {
        if (generatedDestinations.isEmpty() || generatedDestinations.size == 1) {
            return
        }

        codeGenerator.makeFile(
            packageName = codeGenBasePackageName,
            name = "${moduleName}Destinations",
            sourceIds = sourceIds(generatedDestinations).toTypedArray()
        )
            .writeSourceFile(
                packageStatement = moduleDestinationTemplate.packageStatement,
                importableHelper = importableHelper,
                sourceCode = moduleDestinationTemplate.sourceCode
                    .replace(MODULE_DESTINATIONS_PLACEHOLDER, moduleDestinationsCode(generatedDestinations))
                    .replace(MODULE_DESTINATIONS_LIST_NAME_PLACEHOLDER, listName())
                    .replace(REQUIRE_OPT_IN_ANNOTATIONS_PLACEHOLDER, requireOptInAnnotations(generatedDestinations))
            )
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
            code += "@${importableHelper.addAndGetPlaceholder(annotationType)}\n"
        }

        return code.toString()
    }
}