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
    private val argsToSavedStateHandleUtilsWriter: ArgsToSavedStateHandleUtilsWriter,
    private val mermaidGraphWriter: MermaidGraphWriter,
    private val moduleRegistryWriter: ModuleRegistryWriter
) {

    fun write(
        navGraphs: List<RawNavGraphGenParams>,
        destinations: List<CodeGenProcessedDestination>
    ) {
        val navGraphTrees = if (codeGenConfig.generateNavGraphs && navGraphsHaveRoutes(navGraphs, destinations)) {
            val graphTrees = makeNavGraphTrees(navGraphs, destinations)
            navGraphsSingleObjectWriter.write(graphTrees, destinations)
            mermaidGraphWriter.write(graphTrees)

            graphTrees
        } else {
            destinationsListModeWriter.write(destinations)

            emptyList()
        }

        val flattenedNavGraphTrees = navGraphTrees.flatten()
        navArgsGetters.write(destinations, flattenedNavGraphTrees)
        argsToSavedStateHandleUtilsWriter.write(destinations, flattenedNavGraphTrees)

        moduleRegistryWriter.write(destinations, navGraphTrees)
    }

    private fun navGraphsHaveRoutes(
        navGraphs: List<RawNavGraphGenParams>,
        destinations: List<CodeGenProcessedDestination>
    ): Boolean {
        val anyDestinationHasNavGraph = destinations.any { it.navGraphInfo != null }
        val anyNavGraphHasParent = navGraphs.any { it.parent != null }
        val anyNavGraphHasExternalRoutes = navGraphs.any { it.externalRoutes.isNotEmpty() }

        return anyDestinationHasNavGraph || anyNavGraphHasParent || anyNavGraphHasExternalRoutes
    }

    private fun List<RawNavGraphTree>.flatten(): List<RawNavGraphTree> {
        return this + flatMap { it.nestedGraphs.flatten() }
    }
}
