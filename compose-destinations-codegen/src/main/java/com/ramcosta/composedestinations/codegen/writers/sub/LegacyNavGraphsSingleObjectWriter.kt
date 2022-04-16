package com.ramcosta.composedestinations.codegen.writers.sub

import com.ramcosta.composedestinations.codegen.codeGenBasePackageName
import com.ramcosta.composedestinations.codegen.commons.*
import com.ramcosta.composedestinations.codegen.facades.CodeOutputStreamMaker
import com.ramcosta.composedestinations.codegen.facades.Logger
import com.ramcosta.composedestinations.codegen.model.ClassType
import com.ramcosta.composedestinations.codegen.model.GeneratedDestination
import com.ramcosta.composedestinations.codegen.model.NavGraphGeneratingParams
import com.ramcosta.composedestinations.codegen.model.NavGraphInfo
import com.ramcosta.composedestinations.codegen.templates.ADDITIONAL_IMPORTS
import com.ramcosta.composedestinations.codegen.templates.NAV_GRAPHS_PLACEHOLDER
import com.ramcosta.composedestinations.codegen.templates.navGraphsObjectTemplate
import java.io.OutputStream

class LegacyNavGraphsSingleObjectWriter(
    private val codeGenerator: CodeOutputStreamMaker,
    private val logger: Logger,
) {

    private val additionalImports = mutableSetOf<String>()

    fun write(generatedDestinations: List<GeneratedDestination>): List<NavGraphGeneratingParams> {
        val file: OutputStream = codeGenerator.makeFile(
            packageName = codeGenBasePackageName,
            name = GENERATED_NAV_GRAPHS_OBJECT,
            sourceIds = sourceIds(generatedDestinations).toTypedArray()
        )

        val navGraphsParams = generatedDestinations.mapToNavGraphs()
        file += navGraphsObjectTemplate
            .replace(NAV_GRAPHS_PLACEHOLDER, navGraphsDeclaration(navGraphsParams))
            .replace(ADDITIONAL_IMPORTS, additionalImports())

        file.close()

        return navGraphsParams
    }

    private fun additionalImports(): String {
        val imports = StringBuilder()

        additionalImports.sorted().forEachIndexed { idx, it ->
            if (idx == 0) imports += "\n"

            imports += "\nimport $it"
        }

        return imports.toString()
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

    private fun requireOptInAnnotations(navGraphRequireOptInClassTypes: Set<ClassType>): String {
        val code = StringBuilder()

        navGraphRequireOptInClassTypes.forEach { annotationType ->
            additionalImports.add(annotationType.qualifiedName)
            code += "@${annotationType.simpleName}\n\t"
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
        val nestedNavGraphsRequireOptInAnnotations = mutableSetOf<ClassType>()
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
                    requireOptInAnnotationTypes = requireOptInClassTypes
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
                    .apply { addAll(nestedNavGraphsRequireOptInAnnotations) }
            )
        )

        return result
    }

    private fun List<GeneratedDestination>.requireOptInAnnotationClassTypes(): MutableSet<ClassType> {
        val requireOptInClassTypes = flatMapTo(mutableSetOf()) { generatedDest ->
            generatedDest.requireOptInAnnotationTypes
        }
        return requireOptInClassTypes
    }
}