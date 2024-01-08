package com.ramcosta.composedestinations.codegen.commons

import com.ramcosta.composedestinations.codegen.codeGenBasePackageName
import com.ramcosta.composedestinations.codegen.model.CodeGenProcessedDestination
import com.ramcosta.composedestinations.codegen.model.Importable
import com.ramcosta.composedestinations.codegen.model.NavGraphGenParams
import com.ramcosta.composedestinations.codegen.model.Parameter
import com.ramcosta.composedestinations.codegen.model.RawNavArgsClass
import com.ramcosta.composedestinations.codegen.model.RawNavGraphGenParams
import com.ramcosta.composedestinations.codegen.model.Visibility
import com.ramcosta.composedestinations.codegen.writers.sub.navGraphsPackageName

internal data class RawNavGraphTree(
    val rawNavGraphGenParams: RawNavGraphGenParams,
    val destinations: List<CodeGenProcessedDestination>,
    val startRouteArgs: RawNavArgsClass?,
    val requireOptInAnnotationTypes: Set<Importable>,
    val nestedGraphs: List<RawNavGraphTree>,
) : NavGraphGenParams by rawNavGraphGenParams {

    val navGraphImportable: Importable = Importable(
        simpleName = name,
        qualifiedName = "$navGraphsPackageName.$name"
    )

    val genNavArgsType = Importable(
        "${name}NavArgs",
        "$navGraphsPackageName.${name}NavArgs"
    )

    val graphArgsType: Importable? = if (navArgs?.parameters.isNullOrEmpty()) {
        // the graph itself has no arguments
        null
    } else {
        // the graph itself has arguments
        if (startRouteArgs != null) {
            // the graph's start route also has arguments
            // So we're going to generate a new nav args class that has
            // this graph's arguments and also the start route's args class
            genNavArgsType
        } else {
            // the graph's start route has no arguments
            navArgs!!.type
        }
    }

    val navArgTypes: Pair<Importable?, Importable?> = (graphArgsType ?: startRouteArgs?.type) to startRouteArgs?.type

    fun requiresGeneratingNavArgsClass() = navArgTypes.first != null
            && navArgTypes.second != null
            && navArgTypes.first != navArgTypes.second

    fun usesSameArgsAsStartRoute() = navArgTypes.first == navArgTypes.second
            && navArgTypes.second != null

    fun hasNoArgs() = navArgTypes.first == null
            && navArgTypes.second == null

    fun hasOnlyGraphArgs() = navArgTypes.first != null && navArgTypes.second == null
}

internal val setOfPublicStartParticipatingTypes = mutableSetOf<Importable>()

internal fun makeNavGraphTrees(
    navGraphs: List<RawNavGraphGenParams>,
    generatedDestinations: List<CodeGenProcessedDestination>
): List<RawNavGraphTree> {
    val navGraphsByType = navGraphs.associateBy { it.annotationType }

    val destinationsByNavGraphParams: Map<RawNavGraphGenParams, List<CodeGenProcessedDestination>> =
        generatedDestinations.groupBy { destination ->
            navGraphsByType[destination.navGraphInfo.graphType] ?: navGraphs.find { it.default } ?: rootNavGraphGenParams
        }

    val rawNavGraphGenByParent: Map<Importable?, List<RawNavGraphGenParams>> =
        destinationsByNavGraphParams.keys.groupBy { it.parent }

    return (navGraphs + destinationsByNavGraphParams.keys).toSet()
        .filter { it.parent == null }
        .map {
            it.makeGraphTree(
                destinationsByNavGraphParams,
                rawNavGraphGenByParent,
            )
        }
}

internal fun RawNavGraphGenParams.makeGraphTree(
    destinationsByNavGraphParams: Map<RawNavGraphGenParams, List<CodeGenProcessedDestination>>,
    navGraphsByParentType: Map<Importable?, List<RawNavGraphGenParams>>
): RawNavGraphTree {
    val destinations = destinationsByNavGraphParams[this].orEmpty()
    val nestedNavGraphs = navGraphsByParentType[annotationType].orEmpty()

    val nestedGraphs = nestedNavGraphs.map {
        it.makeGraphTree(destinationsByNavGraphParams, navGraphsByParentType)
    }

    return RawNavGraphTree(
        rawNavGraphGenParams = this,
        destinations = destinations,
        startRouteArgs = calculateNavArgsAndValidate(destinations, nestedGraphs),
        requireOptInAnnotationTypes = calculateRequireOptInAnnotationTypes(
            destinations,
            nestedGraphs,
        ) + (includedRoutes.flatMap{ it.requireOptInAnnotationTypes }).toSet(),
        nestedGraphs = nestedGraphs,
    ).also {
        if (visibility == Visibility.PUBLIC) {
            it.addStartRouteTreeToParticipantsOfPublicAPIs()
        }
    }
}

private fun RawNavGraphTree.addStartRouteTreeToParticipantsOfPublicAPIs() {
    val startDestination = destinations.firstOrNull { it.navGraphInfo.start }
    val startNestedGraph = nestedGraphs.firstOrNull { it.isParentStart == true }

    if (includedStartRoute != null) {
        if (startDestination != null || startNestedGraph != null) {
            throw IllegalDestinationsSetup(
                "${annotationType.preferredSimpleName} defines imported route with `start = true` but " +
                        "'${(startDestination?.composableName ?: startNestedGraph?.annotationType?.preferredSimpleName)}' is also defined " +
                        "as its start. Use imported annotations with `start = true` only when start route is not in the current module!"
            )
        }
        // included routes are already generated, so no need to include them in the setOfPublicStartParticipatingTypes
        return
    }

    if (startDestination != null) {
        setOfPublicStartParticipatingTypes.add(startDestination.destinationImportable)
    } else {
        setOfPublicStartParticipatingTypes.add(startNestedGraph!!.annotationType)
        startNestedGraph.addStartRouteTreeToParticipantsOfPublicAPIs()
    }
}

private fun calculateRequireOptInAnnotationTypes(
    destinations: List<CodeGenProcessedDestination>,
    nestedGraphs: List<RawNavGraphTree>
): Set<Importable> {
    return destinations.requireOptInAnnotationClassTypes() + nestedGraphs.requireOptInAnnotationClassTypes()
}

private fun RawNavGraphGenParams.calculateNavArgsAndValidate(
    destinations: List<CodeGenProcessedDestination>,
    nestedGraphs: List<RawNavGraphTree>
): RawNavArgsClass? {
    val startRouteArgsTree = calculateStartRouteNavArgsTree(destinations, nestedGraphs)

    if (visibility == Visibility.PUBLIC) {
        val nonPublicNavArgClasses = startRouteArgsTree.allNavArgs
            .filter { it.second.visibility != Visibility.PUBLIC }
            .filter { !it.second.type.qualifiedName.startsWith(codeGenBasePackageName) }

        if (nonPublicNavArgClasses.isNotEmpty()) {
            throw IllegalDestinationsSetup(
                "[${nonPublicNavArgClasses.joinToString(",") { "'${it.second.type.preferredSimpleName}'" }}] nav arg" +
                        " classes need to be public because they're a part of the public ${annotationType.preferredSimpleName}'s navigation arguments."
            )
        }
    }

    val graphArgNames = navArgs?.parameters?.map { it.name }.orEmpty()
    val startRouteArgNames = startRouteArgsTree.allNavArgs.map { it.first.name }

    val namesInCommon = graphArgNames.intersect(startRouteArgNames.toSet())

    if (namesInCommon.isNotEmpty()) {
        val firstNameInCommon = namesInCommon.first()
        val classWithCollision =
            startRouteArgsTree.allNavArgs.find { it.first.name == firstNameInCommon }!!.second
        throw IllegalDestinationsSetup(
            "${annotationType.preferredSimpleName}: '${navArgs?.type?.preferredSimpleName}' has a field with name '$firstNameInCommon' " +
                    "which is also a field in its start route's class '${classWithCollision.type.preferredSimpleName}'\n" +
                    "The field names of these classes must be unique!"
        )
    }

    return startRouteArgsTree.first()
}

private data class StartRouteArgsTree(
    val graphTree: RawNavGraphTree?,
    val navArgsClass: RawNavArgsClass?,
    val subTree: StartRouteArgsTree?,
) {

    val allNavArgs = parametersRecursive()

    private fun parametersRecursive(): List<Pair<Parameter, RawNavArgsClass>> {
        val currentParams = navArgsClass?.parameters?.map { it to navArgsClass }.orEmpty()
        return currentParams + subTree?.parametersRecursive().orEmpty()
    }

    fun first(): RawNavArgsClass? {
        return if (navArgsClass == null) {
            subTree?.first()
        } else {
            if (subTree?.navArgsClass != null) {
                RawNavArgsClass(
                    parameters = subTree.navArgsClass.parameters,
                    visibility = graphTree!!.visibility,
                    type = graphTree.genNavArgsType
                )
            } else {
                navArgsClass
            }
        }
    }
}

private fun RawNavGraphGenParams.calculateStartRouteNavArgsTree(
    destinations: List<CodeGenProcessedDestination>,
    nestedGraphs: List<RawNavGraphTree>
): StartRouteArgsTree {
    if (includedStartRoute != null) {
        return StartRouteArgsTree(
            navArgsClass = includedStartRoute.navArgs,
            graphTree = null,
            subTree = null
        )
    }

    val startDestination = destinations.find { it.navGraphInfo.start }
    if (startDestination != null) {
        return StartRouteArgsTree(
            navArgsClass = startDestination.navArgsClass,
            subTree = null,
            graphTree = null
        )
    }

    val nestedRawGraphTree = nestedGraphs.find { it.isParentStart == true }
        ?: throw IllegalDestinationsSetup(
            "NavGraph '${annotationType.preferredSimpleName}' doesn't have any start route. " +
                    "Use corresponding annotation with `start = true` in the Destination or nested NavGraph you want to be the start of this graph!"
        )

    return StartRouteArgsTree(
        graphTree = nestedRawGraphTree,
        navArgsClass = nestedRawGraphTree.navArgs,
        subTree = nestedRawGraphTree.rawNavGraphGenParams.calculateStartRouteNavArgsTree(
            nestedRawGraphTree.destinations,
            nestedRawGraphTree.nestedGraphs
        ),
    )
}

@JvmName("requireOptInAnnotationClassTypesRawNavGraphTree")
private fun List<RawNavGraphTree>.requireOptInAnnotationClassTypes(): MutableSet<Importable> {
    return this.flatMapTo(mutableSetOf()) {
        it.requireOptInAnnotationClassTypes() + it.importedNavGraphs.flatMap { it.requireOptInAnnotationTypes }
    }
}

private fun RawNavGraphTree.requireOptInAnnotationClassTypes(): Set<Importable> {
    val nestedGraphAnnotations = nestedGraphs.flatMapTo(mutableSetOf()) {
        it.requireOptInAnnotationClassTypes()
    }

    return destinations.requireOptInAnnotationClassTypes() + nestedGraphAnnotations
}

private fun List<CodeGenProcessedDestination>.requireOptInAnnotationClassTypes(): MutableSet<Importable> {
    val requireOptInClassTypes = flatMapTo(mutableSetOf()) { generatedDest ->
        generatedDest.requireOptInAnnotationTypes
    }
    return requireOptInClassTypes
}