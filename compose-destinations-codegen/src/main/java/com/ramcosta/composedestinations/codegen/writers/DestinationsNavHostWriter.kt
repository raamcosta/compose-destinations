package com.ramcosta.composedestinations.codegen.writers

import com.ramcosta.composedestinations.codegen.commons.*
import com.ramcosta.composedestinations.codegen.facades.CodeOutputStreamMaker
import com.ramcosta.composedestinations.codegen.facades.Logger
import com.ramcosta.composedestinations.codegen.model.AvailableDependencies
import com.ramcosta.composedestinations.codegen.templates.*
import com.ramcosta.composedestinations.codegen.templates.destinationsNavHostTemplate
import java.io.OutputStream

class DestinationsNavHostWriter(
    private val codeGenerator: CodeOutputStreamMaker,
    private val logger: Logger,
    private val availableDependencies: AvailableDependencies
) {

    fun write() {
        val generatedCode = destinationsNavHostTemplate
            .adaptToAccompanistAnimation()
            .adaptToAccompanistMaterial()
            .removeAddComposableElseBlock()
            .replace(DEFAULT_NAV_CONTROLLER_PLACEHOLDER, defaultNavControllerPlaceholder())
            .replace(NAV_HOST_METHOD_NAME, navHostMethodName())
            .replace(ANIMATION_DEFAULT_PARAMS_PLACEHOLDER, defaultAnimationParams())
            .replace(EXPERIMENTAL_API_PLACEHOLDER, experimentalApiPlaceholder())

        val file: OutputStream = codeGenerator.makeFile(
            packageName = PACKAGE_NAME,
            name = DESTINATIONS_NAV_HOST,
        )

        file += generatedCode
        file.close()
    }

    private fun String.adaptToAccompanistAnimation(): String {
        return if (availableDependencies.accompanistAnimation) {
            removeInstancesOf(
                ANIMATED_NAV_HOST_CALL_PARAMETERS_START,
                ANIMATED_NAV_HOST_CALL_PARAMETERS_END,
                INNER_NAV_HOST_CALL_ANIMATED_PARAMETERS_START,
                INNER_NAV_HOST_CALL_ANIMATED_PARAMETERS_END,
                ADD_ANIMATED_COMPOSABLE_START,
                ADD_ANIMATED_COMPOSABLE_END,
                ANIMATED_VISIBILITY_TO_CONTENT_START,
                ANIMATED_VISIBILITY_TO_CONTENT_END
            )
        } else {
            removeFromTo(START_ACCOMPANIST_NAVIGATION_IMPORTS, END_ACCOMPANIST_NAVIGATION_IMPORTS)
                .removeFromTo(ANIMATED_NAV_HOST_CALL_PARAMETERS_START, ANIMATED_NAV_HOST_CALL_PARAMETERS_END)
                .removeFromTo(INNER_NAV_HOST_CALL_ANIMATED_PARAMETERS_START, INNER_NAV_HOST_CALL_ANIMATED_PARAMETERS_END)
                .removeFromTo(ADD_ANIMATED_COMPOSABLE_START, ADD_ANIMATED_COMPOSABLE_END)
                .removeFromTo(ANIMATED_VISIBILITY_TO_CONTENT_START, ANIMATED_VISIBILITY_TO_CONTENT_END)
                .removeFromTo(START_ACCOMPANIST_NAVIGATION, END_ACCOMPANIST_NAVIGATION)
        }
    }

    private fun String.adaptToAccompanistMaterial(): String {
        return if (availableDependencies.accompanistMaterial) {
            removeInstancesOf(ADD_BOTTOM_SHEET_COMPOSABLE_START, ADD_BOTTOM_SHEET_COMPOSABLE_END)
        } else {
            removeFromTo(ADD_BOTTOM_SHEET_COMPOSABLE_START, ADD_BOTTOM_SHEET_COMPOSABLE_END)
                .removeFromTo(START_ACCOMPANIST_MATERIAL_IMPORTS, END_ACCOMPANIST_MATERIAL_IMPORTS)
                .removeFromTo(START_ACCOMPANIST_MATERIAL, END_ACCOMPANIST_MATERIAL)
        }
    }

    private fun String.removeAddComposableElseBlock(): String {
        return if (availableDependencies.accompanistAnimation && availableDependencies.accompanistMaterial) {
            removeFromTo(ADD_COMPOSABLE_WHEN_ELSE_START, ADD_COMPOSABLE_WHEN_ELSE_END)
        } else {
            removeInstancesOf(ADD_COMPOSABLE_WHEN_ELSE_START, ADD_COMPOSABLE_WHEN_ELSE_END)
        }
    }

    private fun defaultNavControllerPlaceholder(): String {
        return if (availableDependencies.accompanistAnimation) "rememberAnimatedNavController"
        else "rememberNavController"
    }

    private fun experimentalApiPlaceholder(): String {
        var result = ""

        if (availableDependencies.accompanistMaterial) {
            result += "@ExperimentalMaterialNavigationApi\n"
        }

        if (availableDependencies.accompanistAnimation) {
            result += "@ExperimentalAnimationApi\n"
        }

        return result
    }

    private fun defaultAnimationParamsPassToInner(): String {
        return if (availableDependencies.accompanistAnimation) {
            """

				defaultAnimationParams = defaultAnimationParams,
            """.trimIndent()
                .prependIndent("\t\t")
        } else {
            ""
        }
    }

    private fun navHostMethodName(): String {
        return if (availableDependencies.accompanistAnimation) {
            "AnimatedNavHost"
        } else {
            "NavHost"
        }
    }

    private fun defaultAnimationParams(): String {
        return if (availableDependencies.accompanistAnimation) {
            """
                
                defaultAnimationParams: DefaultAnimationParams = DefaultAnimationParams(),
            """.trimIndent()
                .prependIndent("\t")
        } else {
            ""
        }
    }
}