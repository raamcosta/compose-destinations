package com.ramcosta.composedestinations.codegen.writers

import com.ramcosta.composedestinations.codegen.commons.makeNavGraphTrees
import com.ramcosta.composedestinations.codegen.model.CodeGenConfig
import com.ramcosta.composedestinations.codegen.model.CodeGenMode
import com.ramcosta.composedestinations.codegen.model.GeneratedDestination
import com.ramcosta.composedestinations.codegen.model.RawNavGraphGenParams
import com.ramcosta.composedestinations.codegen.writers.sub.DestinationsModeWriter
import com.ramcosta.composedestinations.codegen.writers.sub.NavGraphsModeWriter
import com.ramcosta.composedestinations.codegen.writers.sub.NavGraphsSingleObjectWriter
import com.ramcosta.composedestinations.codegen.writers.sub.SingleModuleExtensionsWriter

internal class ModuleOutputWriter(
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
                val graphTrees = makeNavGraphTrees(navGraphs, generatedDestinations)
                navGraphsModeWriter.write(graphTrees)
            }

            is CodeGenMode.Destinations -> {
                destinationsListModeWriter.write(generatedDestinations)
            }

            is CodeGenMode.SingleModule -> {
                val graphTrees = makeNavGraphTrees(navGraphs, generatedDestinations)
                if (codeGenConfig.mode.generateNavGraphs) {
                    navGraphsSingleObjectWriter.write(graphTrees, generatedDestinations)
                } else {
                    // We fallback to just generate a list of all destinations
                    destinationsListModeWriter.write(generatedDestinations)
                }

                singleModuleExtensionsWriter.write()
            }
        }
    }
}
