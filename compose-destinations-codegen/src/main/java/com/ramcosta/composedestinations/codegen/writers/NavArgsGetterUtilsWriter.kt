package com.ramcosta.composedestinations.codegen.writers

import com.ramcosta.composedestinations.codegen.codeGenBasePackageName
import com.ramcosta.composedestinations.codegen.commons.plusAssign
import com.ramcosta.composedestinations.codegen.commons.sourceIds
import com.ramcosta.composedestinations.codegen.facades.CodeOutputStreamMaker
import com.ramcosta.composedestinations.codegen.model.GeneratedDestination
import com.ramcosta.composedestinations.codegen.templates.NAV_ARGS_METHOD_WHEN_CASES
import com.ramcosta.composedestinations.codegen.templates.REQUIRE_OPT_IN_ANNOTATIONS_PLACEHOLDER
import com.ramcosta.composedestinations.codegen.templates.navArgsGettersTemplate
import com.ramcosta.composedestinations.codegen.writers.helpers.ImportableHelper
import com.ramcosta.composedestinations.codegen.writers.helpers.writeSourceFile
import java.io.OutputStream

class NavArgsGettersWriter(
    private val codeGenerator: CodeOutputStreamMaker
) {

    private val importableHelper = ImportableHelper(navArgsGettersTemplate.imports)

    fun write(generatedDestinations: List<GeneratedDestination>) {
        if (generatedDestinations.all { it.navArgsImportable == null }) {
            return
        }

        val file: OutputStream = codeGenerator.makeFile(
            packageName = codeGenBasePackageName,
            name = "NavArgsGetters",
            sourceIds = sourceIds(generatedDestinations).toTypedArray()
        )

        val destinationsWithNavArgs = generatedDestinations.filter { it.navArgsImportable != null }
            .associateBy { it.navArgsImportable!! }
            .values
            .toList()

        file.writeSourceFile(
            packageStatement = navArgsGettersTemplate.packageStatement,
            importableHelper = importableHelper,
            sourceCode = navArgsGettersTemplate.sourceCode
                .replace(NAV_ARGS_METHOD_WHEN_CASES, navArgsMethodWhenCases(destinationsWithNavArgs))
                .replace(REQUIRE_OPT_IN_ANNOTATIONS_PLACEHOLDER, requireOptInAnnotations(destinationsWithNavArgs))
        )
    }

    private fun requireOptInAnnotations(destinationsWithNavArgs: List<GeneratedDestination>): String {
        val requireOptInClassTypes = destinationsWithNavArgs.flatMapTo(mutableSetOf()) { it.requireOptInAnnotationTypes }
        val code = StringBuilder()

        requireOptInClassTypes.forEach { annotationType ->
            code += "@${importableHelper.addAndGetPlaceholder(annotationType)}\n"
        }

        return code.toString()
    }

    private fun navArgsMethodWhenCases(destinationsWithNavArgs: List<GeneratedDestination>): String {
        val sb = StringBuilder()

        destinationsWithNavArgs.forEachIndexed { idx, it ->
            sb += "\t\t${importableHelper.addAndGetPlaceholder(it.navArgsImportable!!)}::class.java " +
                    "-> ${importableHelper.addAndGetPlaceholder(it.destinationImportable)}.argsFrom(savedStateHandle) as T"

            if (idx < destinationsWithNavArgs.lastIndex) {
                sb += "\n"
            }
        }

        return sb.toString()
    }
}
