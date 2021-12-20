package com.ramcosta.composedestinations.codegen.writers

import com.ramcosta.composedestinations.codegen.commons.*
import com.ramcosta.composedestinations.codegen.facades.CodeOutputStreamMaker
import com.ramcosta.composedestinations.codegen.facades.Logger
import com.ramcosta.composedestinations.codegen.model.ClassType
import com.ramcosta.composedestinations.codegen.model.GeneratedDestination
import com.ramcosta.composedestinations.codegen.model.NavGraphGeneratingParams
import com.ramcosta.composedestinations.codegen.templates.ADDITIONAL_IMPORTS
import com.ramcosta.composedestinations.codegen.templates.NAV_GRAPHS_PLACEHOLDER
import com.ramcosta.composedestinations.codegen.templates.navGraphsObjectTemplate
import java.io.OutputStream

class NavGraphsObjectWriter(
    private val codeGenerator: CodeOutputStreamMaker,
    private val logger: Logger,
) {

    private val additionalImports = mutableSetOf<String>()

    fun write(generatedDestinations: List<GeneratedDestination>): List<NavGraphGeneratingParams> {
        val file: OutputStream = codeGenerator.makeFile(
            packageName = PACKAGE_NAME,
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

        val startDestination = startingDestination(route, destinations)

        val destinationsAnchor = "[DESTINATIONS]"
        val nestedGraphsAnchor = "[NESTED_GRAPHS]"
        val requireOptInAnnotationsAnchor = "[REQUIRE_OPT_IN_ANNOTATIONS_ANCHOR]"

        return """
       |    ${requireOptInAnnotationsAnchor}val ${navGraphFieldName(route)} = $GENERATED_NAV_GRAPH(
       |        route = "$route",
       |        startDestination = ${startDestination},
       |        destinations = listOf(
       |            $destinationsAnchor
       |        )${if (nestedNavGraphs.isEmpty()) "" else ",\n|\t\t$nestedGraphsAnchor"}
       |    )
        """.trimMargin()
            .replace(destinationsAnchor, destinationsInsideList(destinations))
            .replace(nestedGraphsAnchor, nestedGraphsList(nestedNavGraphs))
            .replace(requireOptInAnnotationsAnchor, requireOptInAnnotations(requireOptInAnnotationTypes))

    }

    private fun requireOptInAnnotations(navGraphRequireOptInClassTypes: Set<ClassType>): String {
        val code = StringBuilder()

        navGraphRequireOptInClassTypes.forEach { annotationType ->
            additionalImports.add(annotationType.qualifiedName)
            code += "@${annotationType.simpleName}\n\t"
        }

        return code.toString()
    }

    private fun navGraphFieldName(navGraphRoute: String): String {
        val regex = "[^a-zA-Z]".toRegex()
        val auxNavGraphRoute = navGraphRoute.toCharArray().toMutableList()
        var weirdCharIndex = auxNavGraphRoute.indexOfFirst{ it.toString().matches(regex) }

        while(weirdCharIndex != -1) {
            auxNavGraphRoute.removeAt(weirdCharIndex)
            if (weirdCharIndex >= auxNavGraphRoute.size) {
                break
            }
            auxNavGraphRoute[weirdCharIndex] = auxNavGraphRoute[weirdCharIndex].uppercaseChar()

            weirdCharIndex = auxNavGraphRoute.indexOfFirst { it.toString().matches(regex) }
        }

        return String(auxNavGraphRoute.toCharArray())
    }

    private fun startingDestination(navGraphRoute: String, generatedDestinations: List<GeneratedDestination>): String {
        val startingDestinations = generatedDestinations.filter { it.isStartDestination }
        if (startingDestinations.isEmpty()) {
            throw IllegalDestinationsSetup("No start destination found for nav graph $navGraphRoute!")
        }

        if (startingDestinations.size > 1) {
            throw IllegalDestinationsSetup("Found ${startingDestinations.size} start destinations in $navGraphRoute nav graph, only one is allowed!")
        }

        return startingDestinations[0].simpleName
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

    private fun sourceIds(generatedDestinations: List<GeneratedDestination>): MutableList<String> {
        val sourceIds = mutableListOf<String>()
        generatedDestinations.forEach {
            sourceIds.addAll(it.sourceIds)
        }
        return sourceIds
    }

    private fun List<GeneratedDestination>.mapToNavGraphs(): List<NavGraphGeneratingParams> {
        val result = mutableListOf<NavGraphGeneratingParams>()
        val destinationsByNavGraph: MutableMap<String, List<GeneratedDestination>> =
            groupBy { it.navGraphRoute }.toMutableMap()

        val nestedNavGraphs = mutableListOf<String>()
        val rootDestinations = destinationsByNavGraph.remove("root")

        destinationsByNavGraph.forEach {
            val navGraphRoute = it.value[0].navGraphRoute
            nestedNavGraphs.add(navGraphRoute)

            val requireOptInClassTypes = it.value.requireOptInAnnotationClassTypes()
            result.add(NavGraphGeneratingParams(navGraphRoute, it.value, emptyList(), requireOptInClassTypes))
        }

        result.add(NavGraphGeneratingParams("root", rootDestinations.orEmpty(), nestedNavGraphs, rootDestinations.orEmpty().requireOptInAnnotationClassTypes()))

        return result
    }

    private fun List<GeneratedDestination>.requireOptInAnnotationClassTypes(): MutableSet<ClassType> {
        val requireOptInClassTypes = flatMapTo(mutableSetOf()) { generatedDest ->
            generatedDest.requireOptInAnnotationTypes
        }
        return requireOptInClassTypes
    }
}