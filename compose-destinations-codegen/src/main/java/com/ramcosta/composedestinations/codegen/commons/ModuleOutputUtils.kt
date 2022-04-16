package com.ramcosta.composedestinations.codegen.commons

import com.ramcosta.composedestinations.codegen.model.CodeGenConfig
import com.ramcosta.composedestinations.codegen.model.CodeGenMode
import com.ramcosta.composedestinations.codegen.model.GeneratedDestination
import com.ramcosta.composedestinations.codegen.model.RawNavGraphGenParams

internal fun legacyStartingDestination(
    navGraphRoute: String,
    generatedDestinations: List<GeneratedDestination>
): String {
    val startingDestinations = generatedDestinations.filter { it.navGraphInfo.start }
    if (startingDestinations.isEmpty()) {
        throw IllegalDestinationsSetup("Use argument `start = true` in the @Destination annotation of the '$navGraphRoute' nav graph's start destination!")
    }

    if (startingDestinations.size > 1) {
        throw IllegalDestinationsSetup("Found ${startingDestinations.size} start destinations in '$navGraphRoute' nav graph, only one is allowed!")
    }

    return startingDestinations[0].simpleName
}

internal fun startingDestination(
    codeGenConfig: CodeGenConfig,
    navGraphName: String,
    generatedDestinations: List<GeneratedDestination>,
    nestedNavGraphs: List<RawNavGraphGenParams>
): String {
    val startingRouteNames = generatedDestinations.filter { it.navGraphInfo.start }.map { it.simpleName } +
            nestedNavGraphs.filter { it.isParentStart == true }.map {
                if (codeGenConfig.mode is CodeGenMode.NavGraphs) {
                    it.name
                } else {
                    navGraphFieldName(it.route)
                }
            }

    if (startingRouteNames.isEmpty()) {
        throw IllegalDestinationsSetup(
            "NavGraph '$navGraphName' doesn't have any start route. " +
                    "Use corresponding annotation with `start = true` in the Destination or nested NavGraph you want to be the start of this graph!"
        )
    }

    if (startingRouteNames.size > 1) {
        throw IllegalDestinationsSetup("$startingRouteNames are all start routes in '$navGraphName' nav graph, only one is allowed!")
    }

    return startingRouteNames.first()
}

internal fun sourceIds(generatedDestinations: List<GeneratedDestination>): MutableList<String> {
    val sourceIds = mutableListOf<String>()
    generatedDestinations.forEach {
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