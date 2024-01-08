package com.ramcosta.composedestinations.codegen.writers

import com.ramcosta.composedestinations.codegen.commons.RawNavGraphTree
import com.ramcosta.composedestinations.codegen.commons.makeNavGraphTrees
import com.ramcosta.composedestinations.codegen.model.CodeGenConfig
import com.ramcosta.composedestinations.codegen.model.CodeGenProcessedDestination
import com.ramcosta.composedestinations.codegen.model.RawNavGraphGenParams
import com.ramcosta.composedestinations.codegen.model.SubModuleInfo
import com.ramcosta.composedestinations.codegen.writers.sub.DestinationsModeWriter
import com.ramcosta.composedestinations.codegen.writers.sub.NavGraphsSingleObjectWriter

internal class ModuleOutputWriter(
    private val codeGenConfig: CodeGenConfig,
    private val destinationsListModeWriter: DestinationsModeWriter,
    private val navGraphsSingleObjectWriter: NavGraphsSingleObjectWriter,
    private val navArgsGetters: NavArgsGettersWriter,
    private val argsToSavedStateHandleUtilsWriter: ArgsToSavedStateHandleUtilsWriter,
    private val submodules: List<SubModuleInfo>
) {

    fun write(
        navGraphs: List<RawNavGraphGenParams>,
        destinations: List<CodeGenProcessedDestination>
    ) {
        if (codeGenConfig.generateNavGraphs) {
            val graphTrees = makeNavGraphTrees(navGraphs, destinations)
            navGraphsSingleObjectWriter.write(graphTrees, destinations)

            val flattenedNavGraphTrees = graphTrees.flatten()
            navArgsGetters.write(destinations, flattenedNavGraphTrees)
            argsToSavedStateHandleUtilsWriter.write(submodules, destinations, flattenedNavGraphTrees)
        } else {
            // We fallback to just generate a list of all destinations
            destinationsListModeWriter.write(destinations)
            navArgsGetters.write(destinations, emptyList())
            argsToSavedStateHandleUtilsWriter.write(submodules, destinations, emptyList())
        }
    }

    private fun List<RawNavGraphTree>.flatten(): List<RawNavGraphTree> {
        return this + flatMap { it.nestedGraphs.flatten() }
    }
}
