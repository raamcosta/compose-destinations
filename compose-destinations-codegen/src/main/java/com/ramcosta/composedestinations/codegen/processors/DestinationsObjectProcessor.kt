package com.ramcosta.composedestinations.codegen.processors

import com.ramcosta.composedestinations.codegen.commons.*
import com.ramcosta.composedestinations.codegen.facades.CodeOutputStreamMaker
import com.ramcosta.composedestinations.codegen.facades.Logger
import com.ramcosta.composedestinations.codegen.model.GeneratedDestination
import com.ramcosta.composedestinations.codegen.model.ProcessingConfig
import com.ramcosta.composedestinations.codegen.templates.*
import com.ramcosta.composedestinations.codegen.templates.IMPORTS_BLOCK
import com.ramcosta.composedestinations.codegen.templates.destinationsObjectTemplate
import java.io.OutputStream

class DestinationsObjectProcessor(
    private val codeGenerator: CodeOutputStreamMaker,
    private val logger: Logger,
    private val processingConfig: ProcessingConfig
) {

    fun process(generatedDestinations: List<GeneratedDestination>) {
        val sourceIds = mutableListOf<String>()
        generatedDestinations.forEach {
            sourceIds.addAll(it.sourceIds)
        }

        val file: OutputStream = codeGenerator.makeFile(
            packageName = PACKAGE_NAME,
            name = DESTINATIONS_AGGREGATE_CLASS_NAME,
            sourceIds = sourceIds.toTypedArray()
        )

        var generatedCode = destinationsObjectTemplate
            .replace(IMPORTS_BLOCK, importsCode(generatedDestinations))
            .replace(NAV_GRAPHS_DECLARATION, navGraphsDeclaration(generatedDestinations))
            .replace(INNER_NAV_HOST_PLACEHOLDER, if (processingConfig.isAccompanistAnimationAvailable) innerAnimatedNavHost else innerNavHost)
            .replace(DEFAULT_NAV_CONTROLLER_PLACEHOLDER, if (processingConfig.isAccompanistAnimationAvailable) "rememberAnimatedNavController()" else "rememberNavController()")
            .replace(EXPERIMENTAL_API_PLACEHOLDER, if (processingConfig.isAccompanistAnimationAvailable) "\n\t@ExperimentalAnimationApi" else "")
            .replace(ANIMATION_DEFAULT_PARAMS_PLACEHOLDER, animationDefaultParams(processingConfig.isAccompanistAnimationAvailable))
            .replace(ANIMATION_PARAMS_TO_INNER_PLACEHOLDER_1, animationDefaultParamsPassToInner(processingConfig.isAccompanistAnimationAvailable))
            .replace(ANIMATION_PARAMS_TO_INNER_PLACEHOLDER_2, animationDefaultParamsPassToInner(processingConfig.isAccompanistAnimationAvailable).prependIndent("\t"))

        if (!processingConfig.isScaffoldAvailable) {
            val startIndex = generatedCode.indexOf(SCAFFOLD_FUNCTION_START)
            val endIndex = generatedCode.indexOf(SCAFFOLD_FUNCTION_END) + SCAFFOLD_FUNCTION_END.length

            generatedCode = generatedCode.removeRange(startIndex, endIndex)
        }

        file += generatedCode
        file.close()

        val sealedDestSpecFile: OutputStream = codeGenerator.makeFile(
            packageName = PACKAGE_NAME,
            name = GENERATED_DESTINATION
        )

        sealedDestSpecFile += sealedDestinationTemplate.let {
            if (processingConfig.isAccompanistAnimationAvailable) {
                it.replace(TRANSITION_TYPE_START_PLACEHOLDER, "")
                    .replace(TRANSITION_TYPE_END_PLACEHOLDER, "")
            } else {
                it.removeRange(it.indexOf(TRANSITION_TYPE_START_PLACEHOLDER), it.indexOf(TRANSITION_TYPE_END_PLACEHOLDER) + TRANSITION_TYPE_END_PLACEHOLDER.length)
            }
        }

        sealedDestSpecFile.close()
    }

    private fun animationDefaultParamsPassToInner(anyDestinationHasAnimations: Boolean): String {
        return if (anyDestinationHasAnimations) {
            """

				contentAlignment = contentAlignment,
				enterTransition = enterTransition,
				exitTransition = exitTransition,
				popEnterTransition = popEnterTransition,
				popExitTransition = popExitTransition
            """.trimIndent()
                .prependIndent("\t\t\t")
        } else {
            ""
        }
    }

    private fun animationDefaultParams(anyDestinationHasAnimations: Boolean): String {
        return if (anyDestinationHasAnimations) {
            """
                
                contentAlignment: Alignment = Alignment.Center,
                enterTransition: (AnimatedContentScope<String>.(initial: NavBackStackEntry, target: NavBackStackEntry) -> EnterTransition)? =
                    { _, _ -> fadeIn(animationSpec = tween(700)) },
                exitTransition: (AnimatedContentScope<String>.(initial: NavBackStackEntry, target: NavBackStackEntry) -> ExitTransition)? =
                    { _, _ -> fadeOut(animationSpec = tween(700)) },
                popEnterTransition: (AnimatedContentScope<String>.(initial: NavBackStackEntry, target: NavBackStackEntry) -> EnterTransition)? = enterTransition,
                popExitTransition: (AnimatedContentScope<String>.(initial: NavBackStackEntry, target: NavBackStackEntry) -> ExitTransition)? = exitTransition,
            """.trimIndent()
                .prependIndent("\t\t")
        } else {
            ""
        }
    }

    private fun navGraphsDeclaration(generatedDestinations: List<GeneratedDestination>): String {
        val destinationsByNavGraph: MutableMap<String, List<GeneratedDestination>> =
            generatedDestinations
                .groupBy { it.navGraphRoute }
                .toMutableMap()

        val navGraphsDeclaration = StringBuilder()
        val nestedNavGraphs = mutableListOf<String>()

        val rootDestinations = destinationsByNavGraph.remove("root")

        navGraphsDeclaration += "\tobject ${GENERATED_NAV_GRAPH}s {\n\n"

        destinationsByNavGraph.forEach {
            val navGraphRoute = it.value[0].navGraphRoute
            nestedNavGraphs.add(navGraphRoute)

            navGraphsDeclaration += navGraphDeclaration(navGraphRoute, it.value, emptyList())
            navGraphsDeclaration += "\n\n"
        }

        navGraphsDeclaration += navGraphDeclaration("root", rootDestinations!!, nestedNavGraphs)
        navGraphsDeclaration += "\n\t}"

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

        return """
       |        val ${navGraphFieldName(navGraphRoute)} = $GENERATED_NAV_GRAPH(
       |            route = "$navGraphRoute",
       |            startDestination = ${startDestination},
       |            destinations = mapOf(
       |                $destinationsAnchor
       |            )${if (nestedNavGraphs.isEmpty()) "" else ",\n|\t\t\t$nestedGraphsAnchor"}
       |        )
        """.trimMargin()
            .replace(destinationsAnchor, destinationsInsideMap(navGraphDestinations))
            .replace(nestedGraphsAnchor, nestedGraphsList(nestedNavGraphs))

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

    private fun importsCode(qualifiedNames: List<GeneratedDestination>): String {
        val code = StringBuilder()
        qualifiedNames.forEachIndexed { i, it ->
            code += "import ${it.qualifiedName}"
            if (i != qualifiedNames.lastIndex)
                code += "\n"
        }

        return code.toString()
    }

    private fun startingDestination(navGraphRoute: String, generatedDestinations: List<GeneratedDestination>): String {
        val startingDestinations = generatedDestinations.filter { it.isStartDestination }
        if (startingDestinations.isEmpty()) {
            throw RuntimeException("No start destination found for nav graph $navGraphRoute!")
        }

        if (startingDestinations.size > 1) {
            throw RuntimeException("Found ${startingDestinations.size} start destinations in $navGraphRoute nav graph, only one is allowed!")
        }

        return startingDestinations[0].simpleName
    }

    private fun destinationsInsideMap(destinations: List<GeneratedDestination>): String {
        val code = StringBuilder()
        destinations.forEachIndexed { i, it ->
            code += "${it.simpleName}.route to ${it.simpleName}"

            if (i != destinations.lastIndex)
                code += ",\n\t\t\t\t"
        }

        return code.toString()
    }

    private fun nestedGraphsList(navGraphRoutes: List<String>): String {
        val code = StringBuilder()
        navGraphRoutes.forEachIndexed { i, it ->
            if (i == 0) {
                code += "nestedNavGraphs = listOf(\n\t\t\t\t"
            }
            code += navGraphFieldName(it)

            code += if (i != navGraphRoutes.lastIndex)
                ",\n\t\t\t\t"
            else "\n\t\t\t)"
        }

        return code.toString()
    }
}