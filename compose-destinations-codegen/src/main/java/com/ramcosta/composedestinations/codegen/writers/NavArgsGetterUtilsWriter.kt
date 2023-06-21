package com.ramcosta.composedestinations.codegen.writers

import com.ramcosta.composedestinations.codegen.codeGenBasePackageName
import com.ramcosta.composedestinations.codegen.commons.RawNavGraphTree
import com.ramcosta.composedestinations.codegen.commons.plusAssign
import com.ramcosta.composedestinations.codegen.commons.sourceIds
import com.ramcosta.composedestinations.codegen.facades.CodeOutputStreamMaker
import com.ramcosta.composedestinations.codegen.model.CodeGenProcessedDestination
import com.ramcosta.composedestinations.codegen.templates.NAV_ARGS_METHOD_WHEN_CASES
import com.ramcosta.composedestinations.codegen.templates.REQUIRE_OPT_IN_ANNOTATIONS_PLACEHOLDER
import com.ramcosta.composedestinations.codegen.templates.navArgsGettersTemplate
import com.ramcosta.composedestinations.codegen.writers.helpers.ImportableHelper
import com.ramcosta.composedestinations.codegen.writers.helpers.writeSourceFile
import java.io.OutputStream

internal class NavArgsGettersWriter(
    private val codeGenerator: CodeOutputStreamMaker
) {

    private val importableHelper = ImportableHelper(navArgsGettersTemplate.imports)

    fun write(
        generatedDestinations: List<CodeGenProcessedDestination>,
        navGraphTrees: List<RawNavGraphTree>
    ) {
        if (generatedDestinations.all { it.navArgsClass == null }) {
            return
        }

        val file: OutputStream = codeGenerator.makeFile(
            packageName = "$codeGenBasePackageName.navargs",
            name = "NavArgsGetters",
            sourceIds = sourceIds(generatedDestinations, navGraphTrees).toTypedArray()
        )

        val destinationsWithNavArgs = generatedDestinations.filter { it.navArgsClass != null }
            .associateBy { it.navArgsClass!!.type }
            .values
            .toList()

        val navGraphsWithNavArgs = navGraphTrees.filter { it.graphArgs != null }
            .associateBy { it.graphArgs }
            .values
            .toList()

        file.writeSourceFile(
            packageStatement = navArgsGettersTemplate.packageStatement,
            importableHelper = importableHelper,
            sourceCode = navArgsGettersTemplate.sourceCode
                .replace(NAV_ARGS_METHOD_WHEN_CASES, navArgsMethodWhenCases(destinationsWithNavArgs, navGraphsWithNavArgs))
                .replace(REQUIRE_OPT_IN_ANNOTATIONS_PLACEHOLDER, requireOptInAnnotations(destinationsWithNavArgs))
        )
    }

    private fun requireOptInAnnotations(destinationsWithNavArgs: List<CodeGenProcessedDestination>): String {
        val requireOptInClassTypes = destinationsWithNavArgs.flatMapTo(mutableSetOf()) { it.requireOptInAnnotationTypes }
        val code = StringBuilder()

        requireOptInClassTypes.forEach { annotationType ->
            code += "@${importableHelper.addAndGetPlaceholder(annotationType)}\n"
        }

        return code.toString()
    }

    private fun navArgsMethodWhenCases(
        destinationsWithNavArgs: List<CodeGenProcessedDestination>,
        navGraphsWithNavArgs: List<RawNavGraphTree>
    ): String {
        val sb = StringBuilder()

        destinationsWithNavArgs.forEachIndexed { idx, it ->
            sb += "\t\t${importableHelper.addAndGetPlaceholder(it.navArgsClass!!.type)}::class.java " +
                    "-> ${importableHelper.addAndGetPlaceholder(it.destinationImportable)}.argsFrom(argsContainer) as T"

            if (navGraphsWithNavArgs.isNotEmpty() || idx < destinationsWithNavArgs.lastIndex) {
                sb += "\n"
            }
        }

        navGraphsWithNavArgs.forEachIndexed { idx, it ->
            sb += "\t\t${importableHelper.addAndGetPlaceholder(it.graphArgs!!)}::class.java " +
                    "-> ${importableHelper.addAndGetPlaceholder(it.navGraphImportable)}.argsFrom(argsContainer) as T"

            if (idx < navGraphsWithNavArgs.lastIndex) {
                sb += "\n"
            }
        }

        return sb.toString()
    }
}
