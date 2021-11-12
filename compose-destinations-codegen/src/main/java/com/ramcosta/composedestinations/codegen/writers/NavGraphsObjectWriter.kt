package com.ramcosta.composedestinations.codegen.writers

import com.ramcosta.composedestinations.codegen.commons.*
import com.ramcosta.composedestinations.codegen.facades.CodeOutputStreamMaker
import com.ramcosta.composedestinations.codegen.facades.Logger
import com.ramcosta.composedestinations.codegen.model.GeneratedDestination
import com.ramcosta.composedestinations.codegen.templates.NAV_GRAPHS_PLACEHOLDER
import com.ramcosta.composedestinations.codegen.templates.navGraphsObjectTemplate
import java.io.OutputStream

class NavGraphsObjectWriter(
    private val codeGenerator: CodeOutputStreamMaker,
    private val logger: Logger,
) {

    fun write(generatedDestinations: List<GeneratedDestination>) {
        val file: OutputStream = codeGenerator.makeFile(
            packageName = PACKAGE_NAME,
            name = GENERATED_NAV_GRAPHS_OBJECT,
            sourceIds = sourceIds(generatedDestinations).toTypedArray()
        )

        file += navGraphsObjectTemplate
            .replace(NAV_GRAPHS_PLACEHOLDER, navGraphsDeclaration(generatedDestinations))

        file.close()
    }

    private fun navGraphsDeclaration(generatedDestinations: List<GeneratedDestination>): String {
        if (generatedDestinations.isEmpty()) {
            return "\tval root: NavGraph = throw RuntimeException(\"No found destinations for 'root' navigation graph\")"
        }

        val destinationsByNavGraph: MutableMap<String, List<GeneratedDestination>> =
            generatedDestinations
                .groupBy { it.navGraphRoute }
                .toMutableMap()

        val navGraphsDeclaration = StringBuilder()
        val nestedNavGraphs = mutableListOf<String>()

        val rootDestinations = destinationsByNavGraph.remove("root")

        destinationsByNavGraph.forEach {
            val navGraphRoute = it.value[0].navGraphRoute
            nestedNavGraphs.add(navGraphRoute)

            navGraphsDeclaration += navGraphDeclaration(navGraphRoute, it.value, emptyList())
            navGraphsDeclaration += "\n\n"
        }

        navGraphsDeclaration += navGraphDeclaration("root", rootDestinations!!, nestedNavGraphs)

        return navGraphsDeclaration.toString()
    }

    private fun navGraphDeclaration(
        navGraphRoute: String,
        navGraphDestinations: List<GeneratedDestination>,
        nestedNavGraphs: List<String>
    ): String {
        val startDestination = startingDestination(navGraphRoute, navGraphDestinations)

        val destinationsAnchor = "[DESTINATIONS]"
        val nestedGraphsAnchor = "[NESTED_GRAPHS]"
        val requireOptInAnnotationsAnchor = "[REQUIRE_OPT_IN_ANNOTATIONS_ANCHOR]"

        return """
       |    ${requireOptInAnnotationsAnchor}val ${navGraphFieldName(navGraphRoute)} = $GENERATED_NAV_GRAPH(
       |        route = "$navGraphRoute",
       |        startDestination = ${startDestination},
       |        destinations = mapOf(
       |            $destinationsAnchor
       |        )${if (nestedNavGraphs.isEmpty()) "" else ",\n|\t\t$nestedGraphsAnchor"}
       |    )
        """.trimMargin()
            .replace(destinationsAnchor, destinationsInsideMap(navGraphDestinations))
            .replace(nestedGraphsAnchor, nestedGraphsList(nestedNavGraphs))
            .replace(requireOptInAnnotationsAnchor, requireOptInAnnotations(navGraphDestinations))

    }

    private fun requireOptInAnnotations(navGraphDestinations: List<GeneratedDestination>): String {
        val code = StringBuilder()

        navGraphDestinations
            .flatMapTo(mutableSetOf()) { it.requireOptInAnnotationNames }
            .forEach { annotation ->
                code += "@$annotation\n\t"
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

    private fun destinationsInsideMap(destinations: List<GeneratedDestination>): String {
        val code = StringBuilder()
        destinations.forEachIndexed { i, it ->
            code += "${it.simpleName}.route to ${it.simpleName}"

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
}