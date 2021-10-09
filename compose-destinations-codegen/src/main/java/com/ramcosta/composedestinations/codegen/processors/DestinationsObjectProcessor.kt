package com.ramcosta.composedestinations.codegen.processors

import com.ramcosta.composedestinations.codegen.commons.*
import com.ramcosta.composedestinations.codegen.facades.CodeOutputStreamMaker
import com.ramcosta.composedestinations.codegen.facades.Logger
import com.ramcosta.composedestinations.codegen.model.GeneratedDestination
import com.ramcosta.composedestinations.codegen.model.AvailableDependencies
import com.ramcosta.composedestinations.codegen.templates.*
import com.ramcosta.composedestinations.codegen.templates.ADDITIONAL_IMPORTS_BLOCK
import com.ramcosta.composedestinations.codegen.templates.destinationsObjectTemplate
import java.io.OutputStream

class DestinationsObjectProcessor(
    private val codeGenerator: CodeOutputStreamMaker,
    private val logger: Logger,
    private val availableDependencies: AvailableDependencies
) {

    private val additionalImports = mutableSetOf<String>()

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

        if (!availableDependencies.composeMaterial) {
            val startIndex = generatedCode.indexOf(SCAFFOLD_FUNCTION_START)
            val endIndex = generatedCode.indexOf(SCAFFOLD_FUNCTION_END) + SCAFFOLD_FUNCTION_END.length

            generatedCode = generatedCode.removeRange(startIndex, endIndex)

            generatedCode = generatedCode.replace("\nimport androidx.compose.material.*", "")
        }

        if (!availableDependencies.accompanistAnimation) {
            var startIndex = generatedCode.indexOf(START_ACCOMPANIST_NAVIGATION_IMPORTS)
            var endIndex = generatedCode.indexOf(END_ACCOMPANIST_NAVIGATION_IMPORTS) + END_ACCOMPANIST_NAVIGATION_IMPORTS.length

            generatedCode = generatedCode.removeRange(startIndex, endIndex)

            startIndex = generatedCode.indexOf(ANIMATED_NAV_HOST_CALL_PARAMETERS_START)
            endIndex = generatedCode.indexOf(ANIMATED_NAV_HOST_CALL_PARAMETERS_END) + ANIMATED_NAV_HOST_CALL_PARAMETERS_END.length

            generatedCode = generatedCode.removeRange(startIndex, endIndex)

            startIndex = generatedCode.indexOf(INNER_NAV_HOST_CALL_ANIMATED_PARAMETERS_START)
            endIndex = generatedCode.indexOf(INNER_NAV_HOST_CALL_ANIMATED_PARAMETERS_END) + INNER_NAV_HOST_CALL_ANIMATED_PARAMETERS_END.length

            generatedCode = generatedCode.removeRange(startIndex, endIndex)

            startIndex = generatedCode.indexOf(ADD_ANIMATED_COMPOSABLE_START)
            endIndex = generatedCode.indexOf(ADD_ANIMATED_COMPOSABLE_END) + ADD_ANIMATED_COMPOSABLE_END.length

            generatedCode = generatedCode.removeRange(startIndex, endIndex)

            startIndex = generatedCode.indexOf(NAVIGATION_ANIMATION_FUNCTIONS_START)
            endIndex = generatedCode.indexOf(NAVIGATION_ANIMATION_FUNCTIONS_END) + NAVIGATION_ANIMATION_FUNCTIONS_END.length

            generatedCode = generatedCode.removeRange(startIndex, endIndex)
        } else {
            generatedCode = generatedCode
                .replace(ANIMATED_NAV_HOST_CALL_PARAMETERS_START, "")
                .replace(ANIMATED_NAV_HOST_CALL_PARAMETERS_END, "")
                .replace(INNER_NAV_HOST_CALL_ANIMATED_PARAMETERS_START, "")
                .replace(INNER_NAV_HOST_CALL_ANIMATED_PARAMETERS_END, "")
                .replace(ADD_ANIMATED_COMPOSABLE_START, "")
                .replace(ADD_ANIMATED_COMPOSABLE_END, "")
                .replace(NAVIGATION_ANIMATION_FUNCTIONS_START, "")
                .replace(NAVIGATION_ANIMATION_FUNCTIONS_END, "")
        }

        if (availableDependencies.accompanistMaterial) {
            generatedCode = generatedCode
                .replace(ADD_BOTTOM_SHEET_COMPOSABLE_START, "")
                .replace(ADD_BOTTOM_SHEET_COMPOSABLE_END, "")
                .replace(NAVIGATION_BOTTOM_SHEET_FUNCTIONS_START, "")
                .replace(NAVIGATION_BOTTOM_SHEET_FUNCTIONS_END, "")
        } else {
            generatedCode = generatedCode.replace(BOTTOM_SHEET_COMPOSABLE_WRAPPER, "")

            var startIndex = generatedCode.indexOf(ADD_BOTTOM_SHEET_COMPOSABLE_START)
            var endIndex = generatedCode.indexOf(ADD_BOTTOM_SHEET_COMPOSABLE_END) + ADD_BOTTOM_SHEET_COMPOSABLE_END.length

            generatedCode = generatedCode.removeRange(startIndex, endIndex)

            startIndex = generatedCode.indexOf(START_ACCOMPANIST_MATERIAL_IMPORTS)
            endIndex = generatedCode.indexOf(END_ACCOMPANIST_MATERIAL_IMPORTS) + END_ACCOMPANIST_MATERIAL_IMPORTS.length

            generatedCode = generatedCode.removeRange(startIndex, endIndex)

            startIndex = generatedCode.indexOf(NAVIGATION_BOTTOM_SHEET_FUNCTIONS_START)
            endIndex = generatedCode.indexOf(NAVIGATION_BOTTOM_SHEET_FUNCTIONS_END) + NAVIGATION_BOTTOM_SHEET_FUNCTIONS_END.length

            generatedCode = generatedCode.removeRange(startIndex, endIndex)
        }

        if (availableDependencies.accompanistAnimation && availableDependencies.accompanistMaterial) {
            val startIndex = generatedCode.indexOf(ADD_COMPOSABLE_WHEN_ELSE_START)
            val endIndex = generatedCode.indexOf(ADD_COMPOSABLE_WHEN_ELSE_END) + ADD_COMPOSABLE_WHEN_ELSE_END.length

            generatedCode = generatedCode.removeRange(startIndex, endIndex)
        } else {
            generatedCode = generatedCode
                .replace(ADD_COMPOSABLE_WHEN_ELSE_START, "")
                .replace(ADD_COMPOSABLE_WHEN_ELSE_END, "")
        }

        generatedCode = generatedCode
            .replace(NAV_GRAPHS_DECLARATION, navGraphsDeclaration(generatedDestinations))
            .replace(DEFAULT_NAV_CONTROLLER_PLACEHOLDER, if (availableDependencies.accompanistAnimation) "rememberAnimatedNavController()" else "rememberNavController()")
            .replace(NAV_HOST_METHOD_NAME, navHostMethodName())
            .replace(ANIMATION_DEFAULT_PARAMS_PLACEHOLDER, animationDefaultParams())
            .replace(ANIMATION_PARAMS_TO_INNER_PLACEHOLDER_1, animationDefaultParamsPassToInner())
            .replace(ANIMATION_PARAMS_TO_INNER_PLACEHOLDER_2, animationDefaultParamsPassToInner().prependIndent("\t"))
            .replaceEach(EXPERIMENTAL_API_PLACEHOLDER) { if (it > generatedCode.indexOf("//region internals")) experimentalAnimationApi().replace("\t", "") else experimentalAnimationApi() }
            .replace(ADDITIONAL_IMPORTS_BLOCK, importsCode())

        file += generatedCode
        file.close()

        val sealedDestSpecFile: OutputStream = codeGenerator.makeFile(
            packageName = PACKAGE_NAME,
            name = GENERATED_DESTINATION
        )

        sealedDestSpecFile += sealedDestinationTemplate

        sealedDestSpecFile.close()
    }

    private fun navHostMethodName(): String {
        return if (availableDependencies.accompanistAnimation) {
            "AnimatedNavHost"
        } else {
            "NavHost"
        }
    }

    private fun experimentalAnimationApi(): String {
        var result = ""

        if (availableDependencies.accompanistMaterial) {
            result += "@ExperimentalMaterialNavigationApi\n\t"
        }

        if (availableDependencies.accompanistAnimation) {
            result += "@ExperimentalAnimationApi\n\t"
        }

        return result
    }

    private fun importsCode(): String {
        val importsCode = StringBuilder()

        additionalImports.forEach {
            importsCode += "\nimport $it"
        }

        return importsCode.toString()
    }

    private fun animationDefaultParamsPassToInner(): String {
        return if (availableDependencies.accompanistAnimation) {
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

    private fun animationDefaultParams(): String {
        return if (availableDependencies.accompanistAnimation) {
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
        val requireOptInAnnotationsAnchor = "[REQUIRE_OPT_IN_ANNOTATIONS_ANCHOR]"

        return """
       |        ${requireOptInAnnotationsAnchor}val ${navGraphFieldName(navGraphRoute)} = $GENERATED_NAV_GRAPH(
       |            route = "$navGraphRoute",
       |            startDestination = ${startDestination},
       |            destinations = mapOf(
       |                $destinationsAnchor
       |            )${if (nestedNavGraphs.isEmpty()) "" else ",\n|\t\t\t$nestedGraphsAnchor"}
       |        )
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
                code += "@$annotation\n\t\t"
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

    private fun String.replaceEach(toReplace: String, replaceWith: (index: Int) -> String): String {
        var toReturn = this

        while (true) {
            val indexToChange = toReturn.indexOf(toReplace)
            if (indexToChange == -1) break

            toReturn = toReturn.replaceRange(indexToChange, indexToChange + toReplace.length, replaceWith(indexToChange))
        }

        return toReturn
    }
}