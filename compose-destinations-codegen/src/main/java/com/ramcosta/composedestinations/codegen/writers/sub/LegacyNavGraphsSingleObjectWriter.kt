package com.ramcosta.composedestinations.codegen.writers.sub

import com.ramcosta.composedestinations.codegen.codeGenBasePackageName
import com.ramcosta.composedestinations.codegen.commons.*
import com.ramcosta.composedestinations.codegen.facades.CodeOutputStreamMaker
import com.ramcosta.composedestinations.codegen.facades.Logger
import com.ramcosta.composedestinations.codegen.model.GeneratedDestination
import com.ramcosta.composedestinations.codegen.model.Importable
import com.ramcosta.composedestinations.codegen.model.NavGraphGeneratingParams
import com.ramcosta.composedestinations.codegen.model.NavGraphInfo
import com.ramcosta.composedestinations.codegen.templates.NAV_GRAPHS_LIST_PLACEHOLDER
import com.ramcosta.composedestinations.codegen.templates.NAV_GRAPHS_PLACEHOLDER
import com.ramcosta.composedestinations.codegen.templates.navGraphsObjectTemplate
import com.ramcosta.composedestinations.codegen.writers.helpers.ImportableHelper
import com.ramcosta.composedestinations.codegen.writers.helpers.writeSourceFile
import java.util.Collections.addAll

class LegacyNavGraphsSingleObjectWriter(
    private val codeGenerator: CodeOutputStreamMaker,
    private val logger: Logger,
) {

    private val importableHelper = ImportableHelper(navGraphsObjectTemplate.imports)

    fun write(generatedDestinations: List<GeneratedDestination>): List<NavGraphGeneratingParams> {
        val navGraphsParams = generatedDestinations.mapToNavGraphs()
        codeGenerator.makeFile(
            packageName = codeGenBasePackageName,
            name = GENERATED_NAV_GRAPHS_OBJECT,
            sourceIds = sourceIds(generatedDestinations).toTypedArray()
        ).writeSourceFile(
            packageStatement = navGraphsObjectTemplate.packageStatement,
            importableHelper = importableHelper,
            sourceCode = navGraphsObjectTemplate.sourceCode
                .replace(NAV_GRAPHS_PLACEHOLDER, navGraphsDeclaration(navGraphsParams))
                .replace(NAV_GRAPHS_LIST_PLACEHOLDER, navGraphsListDeclaration(navGraphsParams))
        )

        return navGraphsParams
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
        if (route == "root" && destinations.isEmpty()) {
            return "\tval root: NavGraph = throw RuntimeException(\"No found destinations for 'root' navigation graph\")"
        }

        val destinationsAnchor = "[DESTINATIONS]"
        val nestedGraphsAnchor = "[NESTED_GRAPHS]"
        val requireOptInAnnotationsAnchor = "[REQUIRE_OPT_IN_ANNOTATIONS_ANCHOR]"

        val parent = if (route == "root") "null" else "\"${navGraphParams.parent}\""

        return """
       |    ${requireOptInAnnotationsAnchor}val ${navGraphFieldName(route)} = $GENERATED_NAV_GRAPH(
       |        route = "$route",
       |        startRoute = ${startRouteFieldName},
       |        destinations = listOf(
       |            $destinationsAnchor
       |        )${if (nestedNavGraphRoutes.isEmpty()) "" else ",\n|\t\t$nestedGraphsAnchor"},
       |        parent = $parent
       |    )
        """.trimMargin()
            .replace(destinationsAnchor, destinationsInsideList(destinations))
            .replace(nestedGraphsAnchor, nestedGraphsList(nestedNavGraphRoutes))
            .replace(
                requireOptInAnnotationsAnchor,
                requireOptInAnnotations(requireOptInAnnotationTypes)
            )

    }

    private fun navGraphsListDeclaration(navGraphsParams: List<NavGraphGeneratingParams>): String {
        val navGraphsAnchor = "[NAV_GRAPHS]"
        val navGraphFieldNames = navGraphsParams.joinToString(",\n\t\t") {
            navGraphFieldName(it.route)
        }
        return """
       |    val all: List<$GENERATED_NAV_GRAPH> = listOf(
       |        $navGraphsAnchor
       |    )
        """.trimMargin()
            .replace(navGraphsAnchor, navGraphFieldNames)
    }

    private fun requireOptInAnnotations(navGraphRequireOptInImportables: Set<Importable>): String {
        val code = StringBuilder()

        navGraphRequireOptInImportables.forEach { annotationType ->
            code += "@${importableHelper.addAndGetPlaceholder(annotationType)}\n\t"
        }

        return code.toString()
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

    private fun List<GeneratedDestination>.mapToNavGraphs(): List<NavGraphGeneratingParams> {
        val result = mutableListOf<NavGraphGeneratingParams>()
        val destinationsByNavGraph: MutableMap<String, List<GeneratedDestination>> =
            groupBy { (it.navGraphInfo as NavGraphInfo.Legacy).navGraphRoute }.toMutableMap()

        val nestedNavGraphs = mutableListOf<String>()
        val nestedNavGraphsRequireOptInAnnotations = mutableSetOf<Importable>()
        val rootDestinations = destinationsByNavGraph.remove("root")

        destinationsByNavGraph.forEach {
            val navGraphRoute = (it.value[0].navGraphInfo as NavGraphInfo.Legacy).navGraphRoute
            nestedNavGraphs.add(navGraphRoute)

            val requireOptInClassTypes = it.value.requireOptInAnnotationClassTypes()
            nestedNavGraphsRequireOptInAnnotations.addAll(requireOptInClassTypes)

            result.add(
                NavGraphGeneratingParams(
                    route = navGraphRoute,
                    destinations = it.value,
                    startRouteFieldName = legacyStartingDestination(navGraphRoute, it.value),
                    nestedNavGraphRoutes = emptyList(),
                    requireOptInAnnotationTypes = requireOptInClassTypes,
                    parent = it.key
                )
            )
        }

        result.add(
            NavGraphGeneratingParams(
                route = "root",
                destinations = rootDestinations.orEmpty(),
                startRouteFieldName = legacyStartingDestination("root", rootDestinations.orEmpty()),
                nestedNavGraphRoutes = nestedNavGraphs,
                requireOptInAnnotationTypes = rootDestinations.orEmpty()
                    .requireOptInAnnotationClassTypes()
                    .apply { addAll(nestedNavGraphsRequireOptInAnnotations) },
                parent = null
            )
        )

        return result
    }

    private fun List<GeneratedDestination>.requireOptInAnnotationClassTypes(): MutableSet<Importable> {
        val requireOptInClassTypes = flatMapTo(mutableSetOf()) { generatedDest ->
            generatedDest.requireOptInAnnotationTypes
        }
        return requireOptInClassTypes
    }
}