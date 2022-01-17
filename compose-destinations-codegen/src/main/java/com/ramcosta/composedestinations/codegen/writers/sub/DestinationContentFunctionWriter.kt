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

        val parametersToPass = parameters
            .map {
                it.name to resolveArgumentForTypeAndName(it)
            }
            .filter { it.second != null }

        parametersToPass
            .forEachIndexed { i, (name, resolvedArgument) ->
                if (i != 0) {
                    argsCode += ", "
                }

                argsCode += "\n\t\t\t$name = $resolvedArgument"

                if (i == parametersToPass.lastIndex) argsCode += "\n\t\t"
            }

        return argsCode
    }

    private fun resolveArgumentForTypeAndName(parameter: Parameter): String? {
        return when (parameter.type.classType.qualifiedName) {
            NAV_CONTROLLER_QUALIFIED_NAME,
            NAV_HOST_CONTROLLER_QUALIFIED_NAME, -> "navController"
            DESTINATIONS_NAVIGATOR_QUALIFIED_NAME -> "$CORE_NAV_DESTINATIONS_NAVIGATION(navController, navBackStackEntry)"
            NAV_BACK_STACK_ENTRY_QUALIFIED_NAME -> "navBackStackEntry"
            destination.navArgsDelegateType?.qualifiedName -> {
                additionalImports.add("androidx.compose.runtime.remember")
                "remember { argsFrom(navBackStackEntry) }"
            }
            else -> {
                when {
                    navArgs.contains(parameter) -> {
                        parameter.name //this is resolved by argsFrom before the function
                    }

                    !parameter.hasDefault -> {
                        if (parameter.type.classType.qualifiedName != "kotlin.${parameter.type.classType.simpleName}") {
                            additionalImports.add(parameter.type.classType.qualifiedName)
                        }
                        "dependencyContainer.require()"
                    }

                    else -> null
                }
            }
        }
    }
}