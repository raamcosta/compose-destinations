package com.ramcosta.composedestinations.codegen.writers

import com.ramcosta.composedestinations.codegen.commons.makeNavGraphTrees
import com.ramcosta.composedestinations.codegen.model.CodeGenConfig
import com.ramcosta.composedestinations.codegen.model.CodeGenMode
import com.ramcosta.composedestinations.codegen.model.CodeGenProcessedDestination
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
        destinations: List<CodeGenProcessedDestination>
    ) {
        return when (codeGenConfig.mode) {
            is CodeGenMode.NavGraphs -> {
                val graphTrees = makeNavGraphTrees(navGraphs, destinations)
                navGraphsModeWriter.write(graphTrees)
            }

            is CodeGenMode.Destinations -> {
                destinationsListModeWriter.write(destinations)
            }

            is CodeGenMode.SingleModule -> {
                if (codeGenConfig.mode.generateNavGraphs) {
                    val graphTrees = makeNavGraphTrees(navGraphs, destinations)
                    navGraphsSingleObjectWriter.write(graphTrees, destinations)
                } else {
                    // We fallback to just generate a list of all destinations
                    destinationsListModeWriter.write(destinations)
                }

                singleModuleExtensionsWriter.write()
            }
        }
    }
}
