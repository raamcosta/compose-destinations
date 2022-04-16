package com.ramcosta.composedestinations.codegen.writers

import com.ramcosta.composedestinations.codegen.model.*
import com.ramcosta.composedestinations.codegen.writers.sub.*

class ModuleOutputWriter(
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
                    navGraphsModeWriter.write(navGraphs, generatedDestinations)
                } else {
                    legacyNavGraphsModeWriter.write(generatedDestinations)
                }
            }

            is CodeGenMode.Destinations -> {
                destinationsListModeWriter.write(generatedDestinations)
            }

            is CodeGenMode.SingleModule -> {
                val generatedNavGraphs = if (codeGenConfig.mode.generateNavGraphs) {
                    if (usingNavGraphAnnotations) {
                        navGraphsSingleObjectWriter.write(navGraphs, generatedDestinations)
                    } else {
                        legacyNavGraphsSingleObjectWriter.write(generatedDestinations)
                    }
                } else {
                    // We fallback to just generate a list of all destinations
                    destinationsListModeWriter.write(generatedDestinations)
                    emptyList()
                }

                singleModuleExtensionsWriter.write(generatedNavGraphs)
            }
        }
    }
}
