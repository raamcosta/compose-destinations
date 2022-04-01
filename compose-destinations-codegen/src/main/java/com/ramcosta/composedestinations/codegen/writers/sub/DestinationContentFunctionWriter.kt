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

        val (args, needsDependencyContainer) = prepareArguments()
        if (needsDependencyContainer) {
            additionalImports.add("androidx.compose.runtime.remember")
            functionCallCode += "\t\tval dependencyContainer = remember { DestinationDependenciesContainer(this) }\n"
            functionCallCode += "\t\tdependencyContainer.apply { dependenciesContainerBuilder() }\n\n"
        }

        if (navArgs.isNotEmpty() && destination.navArgsDelegateType == null) {
            functionCallCode += "\t\tval (${argNamesInLine()}) = navArgs\n"
        }

        val receiver = prepareReceiver()
        functionCallCode += "\t\t$receiver${composableName}($args)"

        return functionCallCode.toString()
    }

    private fun argNamesInLine(): String {
        return navArgs.joinToString(", ") { it.name }
    }

    private fun prepareReceiver(): String {
        return when (destination.composableReceiverSimpleName) {
            ANIMATED_VISIBILITY_SCOPE_SIMPLE_NAME -> {
                additionalImports.add(ANIMATED_VISIBILITY_SCOPE_QUALIFIED_NAME)
                "val animatedVisibilityScope = (this as $ANIMATED_VISIBILITY_SCOPE_SIMPLE_NAME)\n" +
                        "\t\tanimatedVisibilityScope."
            }

            COLUMN_SCOPE_SIMPLE_NAME -> {
                additionalImports.add(COLUMN_SCOPE_QUALIFIED_NAME)
                "val columnScope = (this as $COLUMN_SCOPE_SIMPLE_NAME)\n" +
                        "\t\tcolumnScope."
            }

            else -> ""
        }
    }

    private fun DestinationGeneratingParams.prepareArguments(): Pair<String, Boolean> {
        var argsCode = ""
        var anyArgNeedsDepContainer = false

        val parametersToPass = parameters
            .map {
                val (arg, argNeedsDepContainer) = resolveArgumentForTypeAndName(it)
                anyArgNeedsDepContainer = anyArgNeedsDepContainer || argNeedsDepContainer

                it.name to arg
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

        return argsCode to anyArgNeedsDepContainer
    }

    private fun resolveArgumentForTypeAndName(parameter: Parameter): Pair<String?, Boolean> {
        var needsDependencyContainer = false
        val arg = when (parameter.type.classType.qualifiedName) {
            NAV_CONTROLLER_QUALIFIED_NAME,
            NAV_HOST_CONTROLLER_QUALIFIED_NAME, -> "navController"
            NAV_BACK_STACK_ENTRY_QUALIFIED_NAME -> "navBackStackEntry"
            DESTINATIONS_NAVIGATOR_QUALIFIED_NAME -> "destinationsNavigator"
            RESULT_RECIPIENT_QUALIFIED_NAME -> {
                additionalImports.add("$CORE_PACKAGE_NAME.scope.resultRecipient")
                "resultRecipient()"
            }
            RESULT_BACK_NAVIGATOR_QUALIFIED_NAME -> {
                additionalImports.add("$CORE_PACKAGE_NAME.scope.resultBackNavigator")
                "resultBackNavigator()"
            }
            destination.navArgsDelegateType?.qualifiedName -> {
                "navArgs"
            }
            else -> {
                when {
                    navArgs.contains(parameter) -> {
                        parameter.name //this is resolved by argsFrom before the function
                    }

                    !parameter.hasDefault -> {
                        needsDependencyContainer = true
                        if (parameter.type.classType.qualifiedName != "kotlin.${parameter.type.classType.simpleName}") {
                            additionalImports.add(parameter.type.classType.qualifiedName)
                        }
                        "dependencyContainer.require()"
                    }

                    else -> null
                }
            }
        }

        return arg to needsDependencyContainer
    }
}