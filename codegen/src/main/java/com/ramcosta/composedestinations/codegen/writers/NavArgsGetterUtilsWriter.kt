package com.ramcosta.composedestinations.codegen.writers

import com.ramcosta.composedestinations.codegen.codeGenBasePackageName
import com.ramcosta.composedestinations.codegen.commons.RawNavGraphTree
import com.ramcosta.composedestinations.codegen.commons.plusAssign
import com.ramcosta.composedestinations.codegen.commons.removeFromTo
import com.ramcosta.composedestinations.codegen.commons.sourceIds
import com.ramcosta.composedestinations.codegen.facades.CodeOutputStreamMaker
import com.ramcosta.composedestinations.codegen.model.CodeGenProcessedDestination
import com.ramcosta.composedestinations.codegen.templates.DESTINATION_ARGS_METHODS_SECTION_END
import com.ramcosta.composedestinations.codegen.templates.DESTINATION_ARGS_METHODS_SECTION_START
import com.ramcosta.composedestinations.codegen.templates.INLINE_DESTINATION_ARGS_METHODS_SECTION_END
import com.ramcosta.composedestinations.codegen.templates.INLINE_DESTINATION_ARGS_METHODS_SECTION_START
import com.ramcosta.composedestinations.codegen.templates.INLINE_NAV_GRAPH_ARGS_METHODS_SECTION_END
import com.ramcosta.composedestinations.codegen.templates.INLINE_NAV_GRAPH_ARGS_METHODS_SECTION_START
import com.ramcosta.composedestinations.codegen.templates.NAV_ARGS_METHOD_WHEN_CASES
import com.ramcosta.composedestinations.codegen.templates.NAV_GRAPH_ARGS_METHODS_SECTION_END
import com.ramcosta.composedestinations.codegen.templates.NAV_GRAPH_ARGS_METHODS_SECTION_START
import com.ramcosta.composedestinations.codegen.templates.NAV_GRAPH_ARGS_METHOD_WHEN_CASES
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
        val noDestinationsWithArgs = generatedDestinations.all { it.navArgsClass == null }
        val noGraphsWithArgs = navGraphTrees.all { it.graphArgsType == null }
        if (noDestinationsWithArgs && noGraphsWithArgs) {
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

        val navGraphsWithNavArgs = navGraphTrees.filter { it.graphArgsType != null }
            .associateBy { it.graphArgsType }
            .values
            .toList()

        file.writeSourceFile(
            packageStatement = navArgsGettersTemplate.packageStatement,
            importableHelper = importableHelper,
            sourceCode = navArgsGettersTemplate.sourceCode
                .run {
                    if (noDestinationsWithArgs) {
                        removeFromTo(INLINE_DESTINATION_ARGS_METHODS_SECTION_START, INLINE_DESTINATION_ARGS_METHODS_SECTION_END)
                            .removeFromTo(DESTINATION_ARGS_METHODS_SECTION_START, DESTINATION_ARGS_METHODS_SECTION_END)
                    } else {
                        replace(INLINE_DESTINATION_ARGS_METHODS_SECTION_START, "")
                            .replace(INLINE_DESTINATION_ARGS_METHODS_SECTION_END, "")
                            .replace(DESTINATION_ARGS_METHODS_SECTION_START, "")
                            .replace(DESTINATION_ARGS_METHODS_SECTION_END, "")
                            .replace(NAV_ARGS_METHOD_WHEN_CASES, navArgsMethodWhenCases(destinationsWithNavArgs))
                    }
                }.run {
                    if (noGraphsWithArgs) {
                        removeFromTo(INLINE_NAV_GRAPH_ARGS_METHODS_SECTION_START, INLINE_NAV_GRAPH_ARGS_METHODS_SECTION_END)
                            .removeFromTo(NAV_GRAPH_ARGS_METHODS_SECTION_START, NAV_GRAPH_ARGS_METHODS_SECTION_END)
                    } else {
                        replace(INLINE_NAV_GRAPH_ARGS_METHODS_SECTION_START, "")
                            .replace(INLINE_NAV_GRAPH_ARGS_METHODS_SECTION_END, "")
                            .replace(NAV_GRAPH_ARGS_METHODS_SECTION_START, "")
                            .replace(NAV_GRAPH_ARGS_METHODS_SECTION_END, "")
                            .replace(NAV_GRAPH_ARGS_METHOD_WHEN_CASES, navGraphArgsMethodWhenCases(navGraphsWithNavArgs))
                    }
                }
                .replace(REQUIRE_OPT_IN_ANNOTATIONS_PLACEHOLDER, requireOptInAnnotations(destinationsWithNavArgs, navGraphsWithNavArgs))
        )
    }

    private fun requireOptInAnnotations(destinationsWithNavArgs: List<CodeGenProcessedDestination>, navGraphs: List<RawNavGraphTree>): String {
        val requireOptInClassTypes = destinationsWithNavArgs.flatMapTo(mutableSetOf()) { it.requireOptInAnnotationTypes } +
                navGraphs.flatMap { it.requireOptInAnnotationTypes }
        val code = StringBuilder()

        requireOptInClassTypes.forEach { annotationType ->
            code += "@${importableHelper.addAndGetPlaceholder(annotationType)}\n"
        }

        return code.toString()
    }

    private fun navArgsMethodWhenCases(
        destinationsWithNavArgs: List<CodeGenProcessedDestination>,
    ): String {
        val sb = StringBuilder()

        destinationsWithNavArgs.forEachIndexed { idx, it ->
            sb += "\t\t${importableHelper.addAndGetPlaceholder(it.navArgsClass!!.type)}::class.java " +
                    "-> ${importableHelper.addAndGetPlaceholder(it.destinationImportable)}"

            if (idx < destinationsWithNavArgs.lastIndex) {
                sb += "\n"
            }
        }

        return sb.toString()
    }

    private fun navGraphArgsMethodWhenCases(
        navGraphsWithNavArgs: List<RawNavGraphTree>,
    ): String {
        val sb = StringBuilder()

        navGraphsWithNavArgs.forEachIndexed { idx, it ->
            sb += "\t\t${importableHelper.addAndGetPlaceholder(it.graphArgsType!!)}::class.java " +
                    "-> ${importableHelper.addAndGetPlaceholder(it.navGraphImportable)}"

            if (idx < navGraphsWithNavArgs.lastIndex) {
                sb += "\n"
            }
        }

        return sb.toString()
    }
}
