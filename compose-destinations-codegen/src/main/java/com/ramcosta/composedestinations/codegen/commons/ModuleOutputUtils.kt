package com.ramcosta.composedestinations.codegen.commons

import com.ramcosta.composedestinations.codegen.model.*
import com.ramcosta.composedestinations.codegen.writers.destinationsPackageName

internal data class RawNavGraphNode(
    val rawNavGraphGenParams: RawNavGraphGenParams,
    val destinations: List<GeneratedDestination>,
    val navArgs: Importable?,
    val requireOptInAnnotationTypes: Set<Importable>,
)

internal data class RawNavGraphTree(
    val node: RawNavGraphNode,
    val nestedGraphs: List<RawNavGraphTree>,
) {
    fun anyGraphCollidesWith(newRootWithModuleName: RawNavGraphGenParams): Boolean {
        val isThisColliding = (node.rawNavGraphGenParams.name == newRootWithModuleName.name
                || node.rawNavGraphGenParams.route == newRootWithModuleName.route)

        if (isThisColliding) return true

        return nestedGraphs.any { it.anyGraphCollidesWith(newRootWithModuleName) }
    }
}

internal fun makeNavGraphTrees(
    navGraphs: List<RawNavGraphGenParams>,
    generatedDestinations: List<GeneratedDestination>
): List<RawNavGraphTree> {
    val defaultNavGraph = navGraphs.find { it.default }
    val navGraphsByType = navGraphs.associateBy { it.type }

    val destinationsByNavGraphParams: Map<RawNavGraphGenParams, List<GeneratedDestination>> =
        generatedDestinations.groupBy {
            if (it.navGraphInfo.isDefault) {
                defaultNavGraph ?: rootNavGraphGenParams
            } else {
                val info = it.navGraphInfo as NavGraphInfo.AnnotatedSource
                navGraphsByType[info.graphType] ?: rootNavGraphGenParams
            }
        }

    val rawNavGraphGenByParent = destinationsByNavGraphParams.keys.groupBy { it.parent }

    return destinationsByNavGraphParams.keys
        .filter { it.parent == null }
        .map {
            it.makeGraphTree(
                destinationsByNavGraphParams,
                rawNavGraphGenByParent,
            )
        }
}

internal fun RawNavGraphGenParams.makeGraphTree(
    destinationsByNavGraphParams: Map<RawNavGraphGenParams, List<GeneratedDestination>>,
    navGraphsByParentType: Map<Importable?, List<RawNavGraphGenParams>>
): RawNavGraphTree {
    val destinations = destinationsByNavGraphParams[this].orEmpty()
    val nestedNavGraphs = navGraphsByParentType[type].orEmpty()

    val nestedGraphs = nestedNavGraphs.map {
        it.makeGraphTree(destinationsByNavGraphParams, navGraphsByParentType)
    }
    return RawNavGraphTree(
        node = RawNavGraphNode(
            rawNavGraphGenParams = this,
            destinations = destinations,
            navArgs = calculateNavArgsAndValidate(destinations, nestedGraphs),
            requireOptInAnnotationTypes = calculateRequireOptInAnnotationTypes(destinations, nestedGraphs),
        ),
        nestedGraphs = nestedGraphs,
    )
}

private fun calculateRequireOptInAnnotationTypes(
    destinations: List<GeneratedDestination>,
    nestedGraphs: List<RawNavGraphTree>
): Set<Importable> {
    return destinations.requireOptInAnnotationClassTypes() + nestedGraphs.requireOptInAnnotationClassTypes()
}

private fun RawNavGraphGenParams.calculateNavArgsAndValidate(
    destinations: List<GeneratedDestination>,
    nestedGraphs: List<RawNavGraphTree>
): Importable? {
    val (navArgs, hasMandatoryArgs) = calculateNavArgsWithMandatoryInfo(destinations, nestedGraphs)

    if (parent == null && navArgs != null && hasMandatoryArgs) {
        val preferredName = if (navArgs.qualifiedName.startsWith(destinationsPackageName)) {
            navArgs.qualifiedName.removePrefix("$destinationsPackageName.")
        } else {
            navArgs.simpleName
        }

        throw IllegalDestinationsSetup(
            "Top level NavGraph '${type.simpleName}' " +
                    "has a start route with mandatory navigation arguments ('$preferredName')!"
        )
    }

    return navArgs
}

private fun calculateNavArgsWithMandatoryInfo(
    destinations: List<GeneratedDestination>,
    nestedGraphs: List<RawNavGraphTree>
): Pair<Importable?, Boolean> {
    val startDestination = destinations.firstOrNull { it.navGraphInfo.start }
    return if (startDestination != null) {
        startDestination.navArgsImportable to startDestination.hasMandatoryNavArgs
    } else {
        val nestedRawGraphTree = nestedGraphs.first { it.node.rawNavGraphGenParams.isParentStart == true }

        calculateNavArgsWithMandatoryInfo(nestedRawGraphTree.node.destinations, nestedRawGraphTree.nestedGraphs)
    }
}

internal fun legacyStartingDestination(
    navGraphRoute: String,
    generatedDestinations: List<GeneratedDestination>
): GeneratedDestination {
    val startingDestinations = generatedDestinations.filter { it.navGraphInfo.start }
    if (startingDestinations.isEmpty()) {
        throw IllegalDestinationsSetup("Use argument `start = true` in the @Destination annotation of the '$navGraphRoute' nav graph's start destination!")
    }

    if (startingDestinations.size > 1) {
        throw IllegalDestinationsSetup("Found ${startingDestinations.size} start destinations in '$navGraphRoute' nav graph, only one is allowed!")
    }

    return startingDestinations[0]
}

internal fun startingDestinationName(
    graphTree: RawNavGraphTree
): String {
    val startingRouteNames = graphTree.node.destinations.filter { it.navGraphInfo.start }.map { it.simpleName } +
            graphTree.nestedGraphs.filter { it.node.rawNavGraphGenParams.isParentStart == true }.map {
                    it.node.rawNavGraphGenParams.name
            }

    if (startingRouteNames.isEmpty()) {
        throw IllegalDestinationsSetup(
            "NavGraph '${graphTree.node.rawNavGraphGenParams.name}' doesn't have any start route. " +
                    "Use corresponding annotation with `start = true` in the Destination or nested NavGraph you want to be the start of this graph!"
        )
    }

    if (startingRouteNames.size > 1) {
        throw IllegalDestinationsSetup("$startingRouteNames are all start routes in '${graphTree.node.rawNavGraphGenParams.name}' nav graph, only one is allowed!")
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

@JvmName("requireOptInAnnotationClassTypesRawNavGraphTree")
private fun List<RawNavGraphTree>.requireOptInAnnotationClassTypes(): MutableSet<Importable> {
    return this.flatMapTo(mutableSetOf()) {
        it.requireOptInAnnotationClassTypes()
    }
}

private fun RawNavGraphTree.requireOptInAnnotationClassTypes(): Set<Importable> {
    val nestedGraphAnnotations = nestedGraphs.flatMapTo(mutableSetOf()) {
        it.requireOptInAnnotationClassTypes()
    }

    return node.destinations.requireOptInAnnotationClassTypes() + nestedGraphAnnotations
}

private fun List<GeneratedDestination>.requireOptInAnnotationClassTypes(): MutableSet<Importable> {
    val requireOptInClassTypes = flatMapTo(mutableSetOf()) { generatedDest ->
        generatedDest.requireOptInAnnotationTypes
    }
    return requireOptInClassTypes
}