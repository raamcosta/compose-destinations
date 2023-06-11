package com.ramcosta.composedestinations.codegen.commons

import com.ramcosta.composedestinations.codegen.model.*
import com.ramcosta.composedestinations.codegen.writers.sub.navGraphsPackageName

internal data class RawNavGraphTree(
    val rawNavGraphGenParams: RawNavGraphGenParams,
    val destinations: List<GeneratedDestination>,
    val startRouteArgs: Importable?,
    val requireOptInAnnotationTypes: Set<Importable>,
    val nestedGraphs: List<RawNavGraphTree>,
): NavGraphGenParams by rawNavGraphGenParams {
    fun anyGraphCollidesWith(newRootWithModuleName: NavGraphGenParams): Boolean {
        val isThisColliding = (rawNavGraphGenParams.name == newRootWithModuleName.name
                || rawNavGraphGenParams.baseRoute == newRootWithModuleName.baseRoute)

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
                navGraphsByType[it.navGraphInfo.graphType] ?: rootNavGraphGenParams
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
        rawNavGraphGenParams = this,
        destinations = destinations,
        startRouteArgs = calculateNavArgsAndValidate(destinations, nestedGraphs),
        requireOptInAnnotationTypes = calculateRequireOptInAnnotationTypes(destinations, nestedGraphs),
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
    val startRouteArgsTree = calculateStartRouteNavArgsTree(type.preferredSimpleName, destinations, nestedGraphs)

    val graphArgNames = navArgs?.parameters?.map { it.name }.orEmpty()
    val startRouteArgNames = startRouteArgsTree.allNavArgs.map { it.first.name }

    val namesInCommon = graphArgNames.intersect(startRouteArgNames.toSet())

    if (namesInCommon.isNotEmpty()) {
        val firstNameInCommon = namesInCommon.first()
        val classWithCollision = startRouteArgsTree.allNavArgs.find { it.first.name == firstNameInCommon }!!.second
        throw IllegalDestinationsSetup(
            "${name}: '${navArgs?.type?.preferredSimpleName}' has a field with name '$firstNameInCommon' " +
                    "which is also a field in its start route's class '${classWithCollision.type.preferredSimpleName}'\n" +
                    "The field names of these classes must be unique!"
        )
    }

    return startRouteArgsTree.first()
}

data class StartRouteArgsTree(
    val navArgsClass: RawNavArgsClass?,
    val subTree: StartRouteArgsTree?,
    private val nestedGraphName: String?,
) {

    val allNavArgs = parametersRecursive()

    private fun parametersRecursive(): List<Pair<Parameter, RawNavArgsClass>> {
        val currentParams = navArgsClass?.parameters?.map { it to navArgsClass }.orEmpty()
        return currentParams + subTree?.parametersRecursive().orEmpty()
    }

    fun first(): Importable? {
        return if (navArgsClass == null) {
            subTree?.first()
        } else {
            if (subTree?.navArgsClass != null) {
                RawNavArgsClass(
                    subTree.navArgsClass.parameters,
                    Importable(
                        "NavArgs",
                        "$navGraphsPackageName.$nestedGraphName.NavArgs"
                    )
                ).type
            } else {
                navArgsClass.type
            }
        }
    }
}

private fun calculateStartRouteNavArgsTree(
    navGraphName: String,
    destinations: List<GeneratedDestination>,
    nestedGraphs: List<RawNavGraphTree>
): StartRouteArgsTree {
    val startDestination = destinations.firstOrNull { it.navGraphInfo.start }
    if (startDestination != null) {
        return StartRouteArgsTree(
            navArgsClass = startDestination.navArgsClass,
            subTree = null,
            nestedGraphName = null
        )
    }

    val nestedRawGraphTree = nestedGraphs.firstOrNull { it.isParentStart == true }
        ?: throw IllegalDestinationsSetup(
            "NavGraph '$navGraphName' doesn't have any start route. " +
                    "Use corresponding annotation with `start = true` in the Destination or nested NavGraph you want to be the start of this graph!"
        )

    return StartRouteArgsTree(
        nestedRawGraphTree.navArgs,
        calculateStartRouteNavArgsTree(nestedRawGraphTree.type.preferredSimpleName, nestedRawGraphTree.destinations, nestedRawGraphTree.nestedGraphs),
        nestedRawGraphTree.name,
    )
}

internal fun startingDestinationName(
    graphTree: RawNavGraphTree
): String {
    val startingRouteNames = graphTree.destinations.filter { it.navGraphInfo.start }.map { it.simpleName } +
            graphTree.nestedGraphs.filter { it.isParentStart == true }.map {
                    it.name
            }

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

    return destinations.requireOptInAnnotationClassTypes() + nestedGraphAnnotations
}

private fun List<GeneratedDestination>.requireOptInAnnotationClassTypes(): MutableSet<Importable> {
    val requireOptInClassTypes = flatMapTo(mutableSetOf()) { generatedDest ->
        generatedDest.requireOptInAnnotationTypes
    }
    return requireOptInClassTypes
}