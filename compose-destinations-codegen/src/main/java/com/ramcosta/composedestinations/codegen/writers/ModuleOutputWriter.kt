package com.ramcosta.composedestinations.codegen.writers

import com.ramcosta.composedestinations.codegen.commons.RawNavGraphTree
import com.ramcosta.composedestinations.codegen.commons.makeNavGraphTrees
import com.ramcosta.composedestinations.codegen.model.CodeGenConfig
import com.ramcosta.composedestinations.codegen.model.CodeGenProcessedDestination
import com.ramcosta.composedestinations.codegen.model.RawNavGraphGenParams
import com.ramcosta.composedestinations.codegen.writers.sub.DestinationsModeWriter
import com.ramcosta.composedestinations.codegen.writers.sub.NavGraphsSingleObjectWriter

internal class ModuleOutputWriter(
    private val codeGenConfig: CodeGenConfig,
    private val destinationsListModeWriter: DestinationsModeWriter,
    private val navGraphsSingleObjectWriter: NavGraphsSingleObjectWriter,
    private val navArgsGetters: NavArgsGettersWriter,
) {

    fun write(
        navGraphs: List<RawNavGraphGenParams>,
        destinations: List<CodeGenProcessedDestination>
    ) {
        if (codeGenConfig.generateNavGraphs) {
            val graphTrees = makeNavGraphTrees(navGraphs, destinations)
            navGraphsSingleObjectWriter.write(graphTrees, destinations)

            navArgsGetters.write(destinations, graphTrees.flatten())
        } else {
            // We fallback to just generate a list of all destinations
            destinationsListModeWriter.write(destinations)
            navArgsGetters.write(destinations, emptyList())
        }
    }

    private fun List<RawNavGraphTree>.flatten(): List<RawNavGraphTree> {
        return this + flatMap { it.nestedGraphs.flatten() }
    }
}
