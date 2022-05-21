package com.ramcosta.composedestinations.codegen.writers.sub

import com.ramcosta.composedestinations.codegen.codeGenBasePackageName
import com.ramcosta.composedestinations.codegen.commons.*
import com.ramcosta.composedestinations.codegen.facades.CodeOutputStreamMaker
import com.ramcosta.composedestinations.codegen.facades.Logger
import com.ramcosta.composedestinations.codegen.model.*
import com.ramcosta.composedestinations.codegen.templates.ADDITIONAL_IMPORTS
import com.ramcosta.composedestinations.codegen.templates.NAV_GRAPHS_PLACEHOLDER
import com.ramcosta.composedestinations.codegen.templates.navGraphsObjectTemplate
import java.io.OutputStream

class NavGraphsSingleObjectWriter(
    private val codeGenerator: CodeOutputStreamMaker,
    private val logger: Logger,
    private val codeGenConfig: CodeGenConfig,
) {

    private val additionalImports = mutableSetOf<String>()

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

        val navGraphsByParentType = navGraphs.groupBy { it.parent }

        val orderedNavGraphGenParams = (navGraphs + rootNavGraphGenParams)
            .sortedByDescending {
                it.distanceToRoot(
                    navGraphsByType.toMutableMap().apply {
                        this[rootNavGraphGenParams.type] = rootNavGraphGenParams
                    }
                )
            }
            .mapNotNull { rawGraph ->

                val destinations = destinationsByNavGraphParams[rawGraph].orEmpty()
                val nestedNavGraphs = navGraphsByParentType[rawGraph.type].orEmpty()

                if (destinations.isEmpty() && nestedNavGraphs.isEmpty()) {
                    return@mapNotNull null
                }

                NavGraphGeneratingParams(
                    route = rawGraph.route,
                    destinations = destinations,
                    startRouteFieldName = startingDestination(codeGenConfig, rawGraph.name, destinations, nestedNavGraphs),
                    nestedNavGraphRoutes = nestedNavGraphs.map { it.route },
                    requireOptInAnnotationTypes = destinations.requireOptInAnnotationClassTypes(),
                )
            }

        writeFile(generatedDestinations, orderedNavGraphGenParams)

        return orderedNavGraphGenParams
    }

    private fun writeFile(
        generatedDestinations: List<GeneratedDestination>,
        orderedNavGraphGenParams: List<NavGraphGeneratingParams>
    ) {
        val file: OutputStream = codeGenerator.makeFile(
            packageName = codeGenBasePackageName,
            name = GENERATED_NAV_GRAPHS_OBJECT,
            sourceIds = sourceIds(generatedDestinations).toTypedArray()
        )

        file += navGraphsObjectTemplate
            .replace(NAV_GRAPHS_PLACEHOLDER, navGraphsDeclaration(orderedNavGraphGenParams))
            .replace(ADDITIONAL_IMPORTS, additionalImports())

        file.close()
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
            additionalImports.add(annotationType.qualifiedName)
            code += "@${annotationType.simpleName}\n\t"
        }

        return code.toString()
    }

    private fun List<GeneratedDestination>.requireOptInAnnotationClassTypes(): MutableSet<Importable> {
        val requireOptInClassTypes = flatMapTo(mutableSetOf()) { generatedDest ->
            generatedDest.requireOptInAnnotationTypes
        }
        return requireOptInClassTypes
    }

    private fun additionalImports(): String {
        val imports = StringBuilder()

        additionalImports.sorted().forEachIndexed { idx, it ->
            if (idx == 0) imports += "\n"

            imports += "\nimport ${it.sanitizePackageName()}"
        }

        return imports.toString()
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
