package com.ramcosta.composedestinations.codegen.writers.sub

import com.ramcosta.composedestinations.codegen.commons.*
import com.ramcosta.composedestinations.codegen.model.DestinationGeneratingParams
import com.ramcosta.composedestinations.codegen.model.Parameter

class DestinationContentFunctionWriter(
    private val destination: DestinationGeneratingParams,
    private val navArgs: List<Parameter>,
    private val additionalImports: MutableSet<String>,
) {

    fun write(): String = with(destination) {
        val functionCallCode = StringBuilder()

        if (navArgs.isNotEmpty() && destination.navArgsDelegateType == null) {
            additionalImports.add("androidx.compose.runtime.remember")
            functionCallCode += "\t\tval (${argNamesInLine()}) = remember { argsFrom(navBackStackEntry) }\n"
        }

        val receiver = prepareReceiver()
        functionCallCode += "\t\t$receiver${composableName}(${prepareArguments()})"

        return functionCallCode.toString()
    }

    private fun argNamesInLine(): String {
        return navArgs.joinToString(", ") { it.name }
    }

    private fun prepareReceiver(): String {
        return when (destination.composableReceiverSimpleName) {
            ANIMATED_VISIBILITY_SCOPE_SIMPLE_NAME -> {
                additionalImports.add(ANIMATED_VISIBILITY_SCOPE_QUALIFIED_NAME)
                "val animatedVisibilityScope = dependencyContainer.require<$ANIMATED_VISIBILITY_SCOPE_SIMPLE_NAME>()\n" +
                        "\t\tanimatedVisibilityScope."
            }

            COLUMN_SCOPE_SIMPLE_NAME -> {
                additionalImports.add(COLUMN_SCOPE_QUALIFIED_NAME)
                "val columnScope = dependencyContainer.require<$COLUMN_SCOPE_SIMPLE_NAME>()\n" +
                        "\t\tcolumnScope."
            }

            else -> ""
        }
    }

    private fun DestinationGeneratingParams.prepareArguments(): String {
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
                    parameter.name //this is resolved by argsFrom before the function

                } else if (!parameter.hasDefault) {
                    if (parameter.type.qualifiedName != "kotlin.${parameter.type.simpleName}") {
                        additionalImports.add(parameter.type.qualifiedName)
                    }
                    "dependencyContainer.require()"
                } else {
                    null
                }
            }
        }
    }
}