package com.ramcosta.composedestinations.codegen.writers

import com.ramcosta.composedestinations.codegen.commons.*
import com.ramcosta.composedestinations.codegen.facades.CodeOutputStreamMaker
import com.ramcosta.composedestinations.codegen.facades.Logger
import com.ramcosta.composedestinations.codegen.model.GeneratedDestination
import com.ramcosta.composedestinations.codegen.model.AvailableDependencies
import com.ramcosta.composedestinations.codegen.templates.*
import com.ramcosta.composedestinations.codegen.templates.ADDITIONAL_IMPORTS_BLOCK
import com.ramcosta.composedestinations.codegen.templates.destinationsObjectTemplate
import java.io.OutputStream

class DestinationsObjectWriter(
    private val codeGenerator: CodeOutputStreamMaker,
    private val logger: Logger,
    private val availableDependencies: AvailableDependencies
) {

    private val additionalImports = mutableSetOf<String>()

    fun write(generatedDestinations: List<GeneratedDestination>) {
        val generatedCode = destinationsObjectTemplate
            .adaptToComposeMaterial()
            .adaptToAccompanistAnimation()
            .adaptToAccompanistMaterial()
            .removeAddComposableElseBlock()
            .removeBottomSheetLayoutWrapper(generatedDestinations)
            .replace(NAV_GRAPHS_DECLARATION, navGraphsDeclaration(generatedDestinations))
            .replace(DEFAULT_NAV_CONTROLLER_PLACEHOLDER, defaultNavControllerPlaceholder())
            .replace(NAV_HOST_METHOD_NAME, navHostMethodName())
            .replace(ANIMATION_DEFAULT_PARAMS_PLACEHOLDER, defaultAnimationParams())
            .replace(BOTTOM_SHEET_DEFAULT_PARAMS_PLACEHOLDER, bottomSheetDefaultParams())
            .replaceEach(ANIMATION_PARAMS_TO_INNER_PLACEHOLDER) { index, str -> defaultAnimationParamsPassToInner(index, str) }
            .replaceEach(EXPERIMENTAL_API_PLACEHOLDER) { index, str -> experimentalApiPlaceholder(index, str) }
            .replace(ADDITIONAL_IMPORTS_BLOCK, importsCode())

        val file: OutputStream = codeGenerator.makeFile(
            packageName = PACKAGE_NAME,
            name = DESTINATIONS_AGGREGATE_CLASS_NAME,
            sourceIds = sourceIds(generatedDestinations).toTypedArray()
        )

        file += generatedCode
        file.close()
    }

    private fun String.adaptToComposeMaterial(): String {
        if (!availableDependencies.composeMaterial) {
            return removeFromTo(SCAFFOLD_FUNCTION_START, SCAFFOLD_FUNCTION_END)
                .removeInstancesOf("\nimport androidx.compose.material.*")
        }

        return this
    }

    private fun String.adaptToAccompanistAnimation(): String {
        return if (availableDependencies.accompanistAnimation) {
            removeInstancesOf(ANIMATED_NAV_HOST_CALL_PARAMETERS_START)
                .removeInstancesOf(ANIMATED_NAV_HOST_CALL_PARAMETERS_END)
                .removeInstancesOf(INNER_NAV_HOST_CALL_ANIMATED_PARAMETERS_START)
                .removeInstancesOf(INNER_NAV_HOST_CALL_ANIMATED_PARAMETERS_END)
                .removeInstancesOf(ADD_ANIMATED_COMPOSABLE_START)
                .removeInstancesOf(ADD_ANIMATED_COMPOSABLE_END)
        } else {
            removeFromTo(START_ACCOMPANIST_NAVIGATION_IMPORTS, END_ACCOMPANIST_NAVIGATION_IMPORTS)
                .removeFromTo(ANIMATED_NAV_HOST_CALL_PARAMETERS_START, ANIMATED_NAV_HOST_CALL_PARAMETERS_END)
                .removeFromTo(INNER_NAV_HOST_CALL_ANIMATED_PARAMETERS_START, INNER_NAV_HOST_CALL_ANIMATED_PARAMETERS_END)
                .removeFromTo(ADD_ANIMATED_COMPOSABLE_START, ADD_ANIMATED_COMPOSABLE_END)
        }
    }

    private fun String.adaptToAccompanistMaterial(): String {
        return if (availableDependencies.accompanistMaterial) {
            removeInstancesOf(ADD_BOTTOM_SHEET_COMPOSABLE_START)
                .removeInstancesOf(ADD_BOTTOM_SHEET_COMPOSABLE_END)
        } else {
            removeInstancesOf(BOTTOM_SHEET_COMPOSABLE_WRAPPER)
                .removeFromTo(ADD_BOTTOM_SHEET_COMPOSABLE_START, ADD_BOTTOM_SHEET_COMPOSABLE_END)
                .removeFromTo(START_ACCOMPANIST_MATERIAL_IMPORTS, END_ACCOMPANIST_MATERIAL_IMPORTS)
        }
    }

    private fun String.removeAddComposableElseBlock(): String {
        return if (availableDependencies.accompanistAnimation && availableDependencies.accompanistMaterial) {
            removeFromTo(ADD_COMPOSABLE_WHEN_ELSE_START, ADD_COMPOSABLE_WHEN_ELSE_END)
        } else {
            removeInstancesOf(ADD_COMPOSABLE_WHEN_ELSE_START)
                .removeInstancesOf(ADD_COMPOSABLE_WHEN_ELSE_END)
        }
    }

    private fun String.removeBottomSheetLayoutWrapper(destinations: List<GeneratedDestination>): String {
        if (destinations.none { it.isBottomSheetStyle }) {
            return removeInstancesOf(BOTTOM_SHEET_COMPOSABLE_WRAPPER)
        }

        return this
    }

    private fun defaultNavControllerPlaceholder(): String {
        return if (availableDependencies.accompanistAnimation) "rememberAnimatedNavController()"
        else "rememberNavController()"
    }

    private fun experimentalApiPlaceholder(replacingIndex: Int, generatedCode: String): String {
        var result = ""

        if (availableDependencies.accompanistMaterial) {
            result += "@ExperimentalMaterialNavigationApi\n\t"
        }

        if (availableDependencies.accompanistAnimation) {
            result += "@ExperimentalAnimationApi\n\t"
        }

        return if (replacingIndex > generatedCode.indexOf("//region internals")) {
            result.removeInstancesOf("\t")
        } else {
            result
        }
    }

    private fun defaultAnimationParamsPassToInner(replacingIndex: Int, generatedCode: String): String {
        val animationParams = if (availableDependencies.accompanistAnimation) {
            """

				defaultAnimationParams = defaultAnimationParams,
            """.trimIndent()
                .prependIndent("\t\t\t")
        } else {
            ""
        }

        return if (replacingIndex > generatedCode.indexOf("Scaffold(")) {
            animationParams.prependIndent("\t")
        } else {
            animationParams
        }
    }

    private fun navHostMethodName(): String {
        return if (availableDependencies.accompanistAnimation) {
            "AnimatedNavHost"
        } else {
            "NavHost"
        }
    }

    private fun importsCode(): String {
        val importsCode = StringBuilder()

        additionalImports.sorted().forEach {
            importsCode += "\nimport $it"
        }

        return importsCode.toString()
    }

    private fun defaultAnimationParams(): String {
        return if (availableDependencies.accompanistAnimation) {
            """
                
                defaultAnimationParams: DefaultAnimationParams = DefaultAnimationParams(),
            """.trimIndent()
                .prependIndent("\t\t")
        } else {
            ""
        }
    }

    private fun bottomSheetDefaultParams(): String {
        return if (availableDependencies.accompanistMaterial) {
            """
                
                bottomSheetParams: @Composable() (BottomSheetLayoutParams.Builder.() -> Unit) = {},
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

    private fun sourceIds(generatedDestinations: List<GeneratedDestination>): MutableList<String> {
        val sourceIds = mutableListOf<String>()
        generatedDestinations.forEach {
            sourceIds.addAll(it.sourceIds)
        }
        return sourceIds
    }
}