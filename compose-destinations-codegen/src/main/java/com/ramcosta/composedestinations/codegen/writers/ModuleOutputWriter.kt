package com.ramcosta.composedestinations.codegen.writers

import com.ramcosta.composedestinations.codegen.commons.makeNavGraphTrees
import com.ramcosta.composedestinations.codegen.model.*
import com.ramcosta.composedestinations.codegen.writers.sub.*

internal class ModuleOutputWriter(
    private val codeGenConfig: CodeGenConfig,
    private val navGraphsModeWriter: NavGraphsModeWriter,
    private val legacyNavGraphsModeWriter: LegacyNavGraphsModeWriter,
    private val destinationsListModeWriter: DestinationsModeWriter,
    private val navGraphsSingleObjectWriter: NavGraphsSingleObjectWriter,
    private val legacyNavGraphsSingleObjectWriter: LegacyNavGraphsSingleObjectWriter,
    private val singleModuleExtensionsWriter: SingleModuleExtensionsWriter
) {

    fun write(
        navGraphs: List<RawNavGraphGenParams>,
        generatedDestinations: List<GeneratedDestination>
    ) {
        val usingNavGraphAnnotations =
            generatedDestinations.any { it.navGraphInfo is NavGraphInfo.AnnotatedSource }

        return when (codeGenConfig.mode) {
            is CodeGenMode.NavGraphs -> {
                if (usingNavGraphAnnotations) {
                    val graphTrees = makeNavGraphTrees(navGraphs, generatedDestinations)
                    navGraphsModeWriter.write(graphTrees)
                } else {
                    legacyNavGraphsModeWriter.write(generatedDestinations)
                }
            }

            is CodeGenMode.Destinations -> {
                destinationsListModeWriter.write(generatedDestinations)
            }

            is CodeGenMode.SingleModule -> {
                val graphTrees = makeNavGraphTrees(navGraphs, generatedDestinations)
                if (codeGenConfig.mode.generateNavGraphs) {
                    if (usingNavGraphAnnotations) {
                        navGraphsSingleObjectWriter.write(graphTrees, generatedDestinations)
                    } else {
                        legacyNavGraphsSingleObjectWriter.write(generatedDestinations)
                    }
                } else {
                    // We fallback to just generate a list of all destinations
                    destinationsListModeWriter.write(generatedDestinations)
                }

                singleModuleExtensionsWriter.write(graphTrees)
            }
        }
    }
}
