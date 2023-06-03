package com.ramcosta.composedestinations.codegen.writers

import com.ramcosta.composedestinations.codegen.model.*
import com.ramcosta.composedestinations.codegen.writers.sub.*

class ModuleOutputWriter(
    private val codeGenConfig: CodeGenConfig,
    private val navGraphsModeWriter: NavGraphsModeWriter,
    private val destinationsListModeWriter: DestinationsModeWriter,
    private val navGraphsSingleObjectWriter: NavGraphsSingleObjectWriter,
    private val singleModuleExtensionsWriter: SingleModuleExtensionsWriter
) {

    fun write(
        navGraphs: List<RawNavGraphGenParams>,
        generatedDestinations: List<GeneratedDestination>
    ) {
        return when (codeGenConfig.mode) {
            is CodeGenMode.NavGraphs -> {
                navGraphsModeWriter.write(navGraphs, generatedDestinations)
            }

            is CodeGenMode.Destinations -> {
                destinationsListModeWriter.write(generatedDestinations)
            }

            is CodeGenMode.SingleModule -> {
                val generatedNavGraphs = if (codeGenConfig.mode.generateNavGraphs) {
                    navGraphsSingleObjectWriter.write(navGraphs, generatedDestinations)
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
