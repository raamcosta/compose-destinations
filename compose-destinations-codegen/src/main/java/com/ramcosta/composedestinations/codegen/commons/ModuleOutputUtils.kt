package com.ramcosta.composedestinations.codegen.commons

import com.ramcosta.composedestinations.codegen.model.CodeGenProcessedDestination
import com.ramcosta.composedestinations.codegen.writers.helpers.ImportableHelper

internal fun ImportableHelper.startingDestinationName(
    graphTree: RawNavGraphTree
): String {
    val startingRouteNames: List<String> = graphTree.destinations.filter { it.navGraphInfo.start }.map { addAndGetPlaceholder(it.destinationImportable) } +
            graphTree.nestedGraphs.filter { it.isParentStart == true }.map { addAndGetPlaceholder(it.navGraphImportable) } +
            graphTree.externalStartRoute?.generatedType?.let { listOf(addAndGetPlaceholder(it)) }.orEmpty()

    if (startingRouteNames.isEmpty()) {
        throw IllegalDestinationsSetup(
            "NavGraph '${graphTree.name}' doesn't have any start route. " +
                    "Use corresponding annotation with `start = true` in the Destination or nested NavGraph you want to be the start of this graph!"
        )
    }

    if (startingRouteNames.size > 1) {
        throw IllegalDestinationsSetup("$startingRouteNames are all start routes in '${graphTree.name}' nav graph, only one is allowed!")
    }

    return startingRouteNames.first()
}

internal fun sourceIds(
    generatedDestinations: List<CodeGenProcessedDestination>,
    navGraphTrees: List<RawNavGraphTree> = emptyList()
): MutableList<String> {
    val sourceIds = mutableListOf<String>()
    generatedDestinations.forEach {
        sourceIds.addAll(it.sourceIds)
    }

    navGraphTrees.forEach {
        sourceIds.addAll(it.sourceIds)
    }

    return sourceIds
}

internal fun navGraphFieldName(navGraphRoute: String): String {
    val regex = "[^a-zA-Z]".toRegex()
    val auxNavGraphRoute = navGraphRoute.toCharArray().toMutableList()
    var weirdCharIndex = auxNavGraphRoute.indexOfFirst { it.toString().matches(regex) }

    while (weirdCharIndex != -1) {
        auxNavGraphRoute.removeAt(weirdCharIndex)
        if (weirdCharIndex >= auxNavGraphRoute.size) {
            break
        }
        auxNavGraphRoute[weirdCharIndex] = auxNavGraphRoute[weirdCharIndex].uppercaseChar()

        weirdCharIndex = auxNavGraphRoute.indexOfFirst { it.toString().matches(regex) }
    }

    return String(auxNavGraphRoute.toCharArray())
}