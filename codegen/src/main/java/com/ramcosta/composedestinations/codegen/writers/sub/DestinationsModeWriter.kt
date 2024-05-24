package com.ramcosta.composedestinations.codegen.writers.sub

import com.ramcosta.composedestinations.codegen.codeGenBasePackageName
import com.ramcosta.composedestinations.codegen.commons.plusAssign
import com.ramcosta.composedestinations.codegen.commons.sourceIds
import com.ramcosta.composedestinations.codegen.facades.CodeOutputStreamMaker
import com.ramcosta.composedestinations.codegen.model.CodeGenProcessedDestination
import com.ramcosta.composedestinations.codegen.moduleName
import com.ramcosta.composedestinations.codegen.templates.MODULE_DESTINATIONS_CLASS_NAME_PLACEHOLDER
import com.ramcosta.composedestinations.codegen.templates.MODULE_DESTINATIONS_PLACEHOLDER
import com.ramcosta.composedestinations.codegen.templates.MODULE_EXTERNAL_DESTINATIONS_PLACEHOLDER
import com.ramcosta.composedestinations.codegen.templates.REQUIRE_OPT_IN_ANNOTATIONS_PLACEHOLDER
import com.ramcosta.composedestinations.codegen.templates.moduleDestinationTemplate
import com.ramcosta.composedestinations.codegen.writers.helpers.ImportableHelper
import com.ramcosta.composedestinations.codegen.writers.helpers.writeSourceFile

internal class DestinationsModeWriter(
    private val codeGenerator: CodeOutputStreamMaker,
) {

    private val importableHelper = ImportableHelper(moduleDestinationTemplate.imports)
    private val className = "${moduleName}ModuleDestinations"

    fun write(generatedDestinations: List<CodeGenProcessedDestination>) {
        if (generatedDestinations.isEmpty()) {
            return
        }

        codeGenerator.makeFile(
            packageName = codeGenBasePackageName,
            name = className,
            sourceIds = sourceIds(generatedDestinations).toTypedArray()
        )
            .writeSourceFile(
                packageStatement = moduleDestinationTemplate.packageStatement,
                importableHelper = importableHelper,
                sourceCode = moduleDestinationTemplate.sourceCode
                    .replace(MODULE_DESTINATIONS_PLACEHOLDER, moduleDestinationsCode(generatedDestinations))
                    .replace(MODULE_DESTINATIONS_CLASS_NAME_PLACEHOLDER, className)
                    .replace(MODULE_EXTERNAL_DESTINATIONS_PLACEHOLDER, externalDestinationAnnotations(generatedDestinations))
                    .replace(REQUIRE_OPT_IN_ANNOTATIONS_PLACEHOLDER, requireOptInAnnotations(generatedDestinations))
            )
    }

    private fun externalDestinationAnnotations(generatedDestinations: List<CodeGenProcessedDestination>): String {
        return "\t@GeneratedCodeExternalDestinations([\n" + generatedDestinations.joinToString("\n") {
            "\t\t${importableHelper.addAndGetPlaceholder(it.destinationImportable)}::class,"
        } + "\n\t])"
    }

    private fun moduleDestinationsCode(generatedDestinations: List<CodeGenProcessedDestination>): String {
        val code = StringBuilder()
        generatedDestinations.forEachIndexed { idx, it ->
            code += "\t\t${it.destinationImportable.simpleName}"

            if (idx != generatedDestinations.lastIndex) {
                code += ",\n"
            }
        }

        return code.toString()
    }

    private fun requireOptInAnnotations(generatedDestinations: List<CodeGenProcessedDestination>): String {
        val requireOptInClassTypes = generatedDestinations.flatMapTo(mutableSetOf()) { it.requireOptInAnnotationTypes }
        val code = StringBuilder()

        requireOptInClassTypes.forEach { annotationType ->
            code += "@${importableHelper.addAndGetPlaceholder(annotationType)}\n"
        }

        return code.toString()
    }
}