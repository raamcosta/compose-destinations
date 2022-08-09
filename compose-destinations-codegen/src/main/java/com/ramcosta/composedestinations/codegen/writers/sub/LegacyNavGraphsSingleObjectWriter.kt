package com.ramcosta.composedestinations.codegen.writers.sub

import com.ramcosta.composedestinations.codegen.codeGenBasePackageName
import com.ramcosta.composedestinations.codegen.commons.*
import com.ramcosta.composedestinations.codegen.facades.CodeOutputStreamMaker
import com.ramcosta.composedestinations.codegen.model.GeneratedDestination
import com.ramcosta.composedestinations.codegen.model.Importable
import com.ramcosta.composedestinations.codegen.model.LegacyNavGraphGeneratingParams
import com.ramcosta.composedestinations.codegen.model.NavGraphInfo
import com.ramcosta.composedestinations.codegen.templates.NAV_GRAPHS_PLACEHOLDER
import com.ramcosta.composedestinations.codegen.templates.navGraphsObjectTemplate
import com.ramcosta.composedestinations.codegen.writers.helpers.ImportableHelper
import com.ramcosta.composedestinations.codegen.writers.helpers.writeSourceFile

class LegacyNavGraphsSingleObjectWriter(
    private val codeGenerator: CodeOutputStreamMaker,
) {

    private val importableHelper = ImportableHelper(navGraphsObjectTemplate.imports)

    fun write(generatedDestinations: List<GeneratedDestination>): List<LegacyNavGraphGeneratingParams> {
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
        )

        return navGraphsParams
    }

    private fun navGraphsDeclaration(navGraphsParams: List<LegacyNavGraphGeneratingParams>): String {
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
        navGraphParams: LegacyNavGraphGeneratingParams
    ): String = with(navGraphParams) {
        if (route == "root" && destinations.isEmpty()) {
            return "\tval root: NavGraph = throw RuntimeException(\"No found destinations for 'root' navigation graph\")"
        }

        val destinationsAnchor = "[DESTINATIONS]"
        val nestedGraphsAnchor = "[NESTED_GRAPHS]"
        val requireOptInAnnotationsAnchor = "[REQUIRE_OPT_IN_ANNOTATIONS_ANCHOR]"

//        val navGraphClassName =
//            if (navGraphParams.startRouteHasNavArgs) GENERATED_TYPED_NAV_GRAPH else GENERATED_DIRECTION_NAV_GRAPH
//TODO RACOSTA
        return """
       |    ${requireOptInAnnotationsAnchor}val ${navGraphFieldName(route)} = ERROR(
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

    private fun List<GeneratedDestination>.mapToNavGraphs(): List<LegacyNavGraphGeneratingParams> {
        val result = mutableListOf<LegacyNavGraphGeneratingParams>()
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

            val startDestination = legacyStartingDestination(navGraphRoute, it.value)
            result.add(
                LegacyNavGraphGeneratingParams(
                    route = navGraphRoute,
                    destinations = it.value,
                    startRouteFieldName = startDestination.simpleName,
                    nestedNavGraphRoutes = emptyList(),
                    requireOptInAnnotationTypes = requireOptInClassTypes
                )
            )
        }

        result.add(
            LegacyNavGraphGeneratingParams(
                route = "root",
                destinations = rootDestinations.orEmpty(),
                startRouteFieldName = legacyStartingDestination("root", rootDestinations.orEmpty()).simpleName,
                nestedNavGraphRoutes = nestedNavGraphs,
                requireOptInAnnotationTypes = rootDestinations.orEmpty()
                    .requireOptInAnnotationClassTypes()
                    .apply { addAll(nestedNavGraphsRequireOptInAnnotations) }
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