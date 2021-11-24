package com.ramcosta.composedestinations.codegen.writers.sub

import com.ramcosta.composedestinations.codegen.commons.*
import com.ramcosta.composedestinations.codegen.model.*

class DestinationContentFunctionWriter(
    private val destination: Destination,
    private val navArgs: List<Parameter>,
    private val additionalImports: MutableSet<String>,
) {

    fun write(): String = with(destination) {
        val functionCallCode = StringBuilder()

        if (hasContainerArgument()) {
            functionCallCode += "\t\tval container = DestinationDependenciesContainer().apply { dependenciesContainerBuilder() }\n"
        }

        val receiver = prepareReceiver()
        functionCallCode += "\t\t$receiver${composableName}(${prepareArguments()})"

        return functionCallCode.toString()
    }

    private fun prepareReceiver(): String {
        return when (destination.composableReceiverSimpleName) {
            ANIMATED_VISIBILITY_SCOPE_SIMPLE_NAME -> {
                additionalImports.add(ANIMATED_VISIBILITY_SCOPE_QUALIFIED_NAME)
                "val animatedVisibilityScope = container.require<$ANIMATED_VISIBILITY_SCOPE_SIMPLE_NAME>()\n" +
                        "\t\tanimatedVisibilityScope."
            }

            COLUMN_SCOPE_SIMPLE_NAME -> {
                additionalImports.add(COLUMN_SCOPE_QUALIFIED_NAME)
                "val columnScope = container.require<$COLUMN_SCOPE_SIMPLE_NAME>()\n" +
                        "\t\tcolumnScope."
            }

            else -> ""
        }
    }

    private fun Destination.prepareArguments(): String {
        var argsCode = ""

        parameters.forEachIndexed { i, it ->

            val resolvedArgument = resolveArgumentForTypeAndName(it)

            if (resolvedArgument != null) {
                if (i != 0) argsCode += ", "

                argsCode += "\n\t\t\t${it.name} = $resolvedArgument"

            } else if (!it.hasDefault) {
                throw IllegalDestinationsSetup("Composable: $composableName - Unresolvable argument type without default value: $it")
            }

            if (i == parameters.lastIndex) argsCode += "\n\t\t"
        }

        return argsCode
    }

    private fun resolveArgumentForTypeAndName(parameter: Parameter): String? {
        return when (parameter.type.qualifiedName) {
            NAV_CONTROLLER_QUALIFIED_NAME,
            NAV_HOST_CONTROLLER_QUALIFIED_NAME, -> "navController"
            DESTINATIONS_NAVIGATOR_QUALIFIED_NAME -> "$CORE_NAV_DESTINATIONS_NAVIGATION(navController, navBackStackEntry)"
            NAV_BACK_STACK_ENTRY_QUALIFIED_NAME -> "navBackStackEntry"
            destination.navArgsDelegateType?.qualifiedName -> {
                additionalImports.add("androidx.compose.runtime.remember")
                "remember { argsFrom(navBackStackEntry) }"
            }
            else -> {
                if (navArgs.contains(parameter)) {
                    NavArgResolver.resolve(destination, additionalImports, parameter, true)

                } else if (!parameter.hasDefault) {
                    if (parameter.type.qualifiedName != "kotlin.${parameter.type.simpleName}") {
                        additionalImports.add(parameter.type.qualifiedName)
                    }
                    "container.require()"
                } else {
                    null
                }
            }
        }
    }

    private fun hasContainerArgument(): Boolean {
        if (destination.composableReceiverSimpleName
            in arrayOf(COLUMN_SCOPE_SIMPLE_NAME, ANIMATED_VISIBILITY_SCOPE_SIMPLE_NAME)) {
            return true
        }

        return destination.parameters.any { param ->
            !param.hasDefault
            && param !in navArgs
            && param.type.qualifiedName !in arrayOf(NAV_CONTROLLER_QUALIFIED_NAME, NAV_HOST_CONTROLLER_QUALIFIED_NAME, DESTINATIONS_NAVIGATOR_QUALIFIED_NAME, NAV_BACK_STACK_ENTRY_QUALIFIED_NAME)
            && param.type.qualifiedName != destination.navArgsDelegateType?.qualifiedName
        }
    }
}