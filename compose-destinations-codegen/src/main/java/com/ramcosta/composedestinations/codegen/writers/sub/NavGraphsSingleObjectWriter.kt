package com.ramcosta.composedestinations.codegen.writers.sub

import com.ramcosta.composedestinations.codegen.codeGenBasePackageName
import com.ramcosta.composedestinations.codegen.commons.*
import com.ramcosta.composedestinations.codegen.facades.CodeOutputStreamMaker
import com.ramcosta.composedestinations.codegen.model.*
import com.ramcosta.composedestinations.codegen.templates.NAV_GRAPHS_PLACEHOLDER
import com.ramcosta.composedestinations.codegen.templates.navGraphsObjectTemplate
import com.ramcosta.composedestinations.codegen.writers.helpers.ImportableHelper
import com.ramcosta.composedestinations.codegen.writers.helpers.writeSourceFile

class NavGraphsSingleObjectWriter(
    private val codeGenerator: CodeOutputStreamMaker,
    private val codeGenConfig: CodeGenConfig,
) {

    private val importableHelper = ImportableHelper(navGraphsObjectTemplate.imports)

    fun write(
        navGraphs: List<RawNavGraphGenParams>,
        generatedDestinations: List<GeneratedDestination>
    ): List<NavGraphGeneratingParams> {
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

        val allNavGraphs = navGraphs + rootNavGraphGenParams
        val allNavGraphsByParentType = allNavGraphs.groupBy { it.parent }

        val relevantNavGraphs = allNavGraphs.filter {
            it.isNotEmptyRecursively(destinationsByNavGraphParams, allNavGraphsByParentType)
        }
        val relevantNavGraphsByParentType = relevantNavGraphs.groupBy { it.parent }

        val orderedNavGraphGenParams = relevantNavGraphs
            .sortedByDescending {
                it.distanceToRoot(
                    navGraphsByType.toMutableMap().apply {
                        this[rootNavGraphGenParams.type] = rootNavGraphGenParams
                    }
                )
            }
            .map { rawGraph ->

                val destinations = destinationsByNavGraphParams[rawGraph].orEmpty()
                val nestedNavGraphs = relevantNavGraphsByParentType[rawGraph.type].orEmpty()

                NavGraphGeneratingParamsImpl(
                    rawParams = rawGraph,
                    route = rawGraph.route,
                    destinations = destinations,
                    startRouteFieldName = startingRoute(codeGenConfig, rawGraph.name, destinations, nestedNavGraphs),
                    nestedNavGraphRoutes = nestedNavGraphs.map { it.route },
                    requireOptInAnnotationTypes = destinations.requireOptInAnnotationClassTypes()
                        .apply {
                            nestedNavGraphs.forEach {
                                addAll(destinationsByNavGraphParams[it].orEmpty().requireOptInAnnotationClassTypes())
                            }
                        },
                )
            }

        checkUniquenessOnNavGraphFieldNames(orderedNavGraphGenParams)

        writeFile(generatedDestinations, orderedNavGraphGenParams)

        return orderedNavGraphGenParams
    }

    private fun writeFile(
        generatedDestinations: List<GeneratedDestination>,
        orderedNavGraphGenParams: List<NavGraphGeneratingParams>
    ) {
        codeGenerator.makeFile(
            packageName = codeGenBasePackageName,
            name = GENERATED_NAV_GRAPHS_OBJECT,
            sourceIds = sourceIds(generatedDestinations).toTypedArray()
        ).writeSourceFile(
            packageStatement = navGraphsObjectTemplate.packageStatement,
            importableHelper = importableHelper,
            sourceCode = navGraphsObjectTemplate.sourceCode
                .replace(NAV_GRAPHS_PLACEHOLDER, navGraphsDeclaration(orderedNavGraphGenParams))

        )
    }

    private fun RawNavGraphGenParams.isNotEmptyRecursively(
        destinationsByNavGraphParams: Map<RawNavGraphGenParams, List<GeneratedDestination>>,
        navGraphsByParentType: Map<Importable?, List<RawNavGraphGenParams>>
    ): Boolean {
        val dest = destinationsByNavGraphParams[this].orEmpty()
        val nested = navGraphsByParentType[this.type].orEmpty()

        return dest.isNotEmpty() || nested.any {
            it.isNotEmptyRecursively(destinationsByNavGraphParams, navGraphsByParentType)
        }
    }

    private fun navGraphsDeclaration(navGraphsParams: List<NavGraphGeneratingParams>): String {
        val navGraphsDeclaration = StringBuilder()

        navGraphsParams.forEachIndexed { idx, navGraphParams ->
            navGraphsDeclaration += navGraphDeclaration(navGraphParams)

            if (idx != navGraphsParams.lastIndex) {
                navGraphsDeclaration += "\n\n"
            }
        }

        return navGraphsDeclaration.toString()
    }

    private fun checkUniquenessOnNavGraphFieldNames(navGraphsParams: List<NavGraphGeneratingParamsImpl>) {
        val nonUniqueFieldNames = navGraphsParams.groupBy { navGraphFieldName(it.route) }
            .filter {
                it.value.size > 1
            }.flatMap {
                it.value
            }.map {
                it.rawParams.type.simpleName
            }

        if (nonUniqueFieldNames.isNotEmpty()) {
            throw IllegalDestinationsSetup(
                "NavGraphs $nonUniqueFieldNames result in the same field for the NavGraphs " +
                        "final object. Use only letters in your NavGraph annotations!"
            )
        }
    }

    private fun navGraphDeclaration(
        navGraphParams: NavGraphGeneratingParams
    ): String = with(navGraphParams) {

        val destinationsAnchor = "[DESTINATIONS]"
        val nestedGraphsAnchor = "[NESTED_GRAPHS]"
        val requireOptInAnnotationsAnchor = "[REQUIRE_OPT_IN_ANNOTATIONS_ANCHOR]"

        return """
       |    ${requireOptInAnnotationsAnchor}val ${navGraphFieldName(route)} = $GENERATED_NAV_GRAPH(
       |        route = "$route",
       |        startRoute = ${startRouteFieldName},
       |        destinations = listOf(
       |            $destinationsAnchor
       |        )${if (nestedNavGraphRoutes.isEmpty()) "" else ",\n|\t\t$nestedGraphsAnchor"}
       |    )
        """.trimMargin()
            .replace(destinationsAnchor, destinationsInsideList(destinations))
            .replace(nestedGraphsAnchor, nestedGraphsList(nestedNavGraphRoutes))
            .replace(
                requireOptInAnnotationsAnchor,
                requireOptInAnnotations(requireOptInAnnotationTypes)
            )

    }

    private fun destinationsInsideList(destinations: List<GeneratedDestination>): String {
        val code = StringBuilder()
        destinations.forEachIndexed { i, it ->
            code += it.simpleName

            if (i != destinations.lastIndex)
                code += ",\n\t\t\t"
        }

        return code.toString()
    }

    private fun nestedGraphsList(navGraphRoutes: List<String>): String {
        val code = StringBuilder()
        navGraphRoutes.forEachIndexed { i, it ->
            if (i == 0) {
                code += "nestedNavGraphs = listOf(\n\t\t\t"
            }
            code += navGraphFieldName(it)

            code += if (i != navGraphRoutes.lastIndex)
                ",\n\t\t\t"
            else "\n\t\t)"
        }

        return code.toString()
    }

    private fun requireOptInAnnotations(navGraphRequireOptInImportables: Set<Importable>): String {
        val code = StringBuilder()

        navGraphRequireOptInImportables.forEach { annotationType ->
            code += "@${importableHelper.addAndGetPlaceholder(annotationType)}\n\t"
        }

        return code.toString()
    }

    private fun List<GeneratedDestination>.requireOptInAnnotationClassTypes(): MutableSet<Importable> {
        val requireOptInClassTypes = flatMapTo(mutableSetOf()) { generatedDest ->
            generatedDest.requireOptInAnnotationTypes
        }
        return requireOptInClassTypes
    }

    private fun RawNavGraphGenParams.distanceToRoot(
        navGraphsByType: Map<Importable, RawNavGraphGenParams>
    ): Int {
        var distance = 0
        var parent: RawNavGraphGenParams? = parent?.let { navGraphsByType[it]!! }

        while (true) {
            if (parent == null) {
                break
            }
            parent = parent.parent?.let { navGraphsByType[it]!! }

            distance++
        }

        return distance
    }
}
