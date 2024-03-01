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
    private val mermaidGraphWriter: MermaidGraphWriter,
    private val moduleRegistryWriter: ModuleRegistryWriter,
    private val submodules: List<SubModuleInfo>
) {

    fun write(
        navGraphs: List<RawNavGraphGenParams>,
        destinations: List<CodeGenProcessedDestination>
    ) {
        val navGraphTrees = if (codeGenConfig.generateNavGraphs && navGraphs.isNotEmpty()) {
            val graphTrees = makeNavGraphTrees(navGraphs, destinations)
            navGraphsSingleObjectWriter.write(graphTrees, destinations)
            mermaidGraphWriter.write(submodules, graphTrees)

            graphTrees
        } else {
            destinationsListModeWriter.write(destinations)

            emptyList()
        }

        val flattenedNavGraphTrees = navGraphTrees.flatten()
        navArgsGetters.write(destinations, flattenedNavGraphTrees)
        argsToSavedStateHandleUtilsWriter.write(submodules, destinations, flattenedNavGraphTrees)

        moduleRegistryWriter.write(destinations, navGraphTrees)
    }

    private fun List<RawNavGraphTree>.flatten(): List<RawNavGraphTree> {
        return this + flatMap { it.nestedGraphs.flatten() }
    }
}
