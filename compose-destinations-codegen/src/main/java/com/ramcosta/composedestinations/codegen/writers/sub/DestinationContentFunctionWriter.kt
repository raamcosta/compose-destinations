package com.ramcosta.composedestinations.codegen.writers.sub

import com.ramcosta.composedestinations.codegen.commons.*
import com.ramcosta.composedestinations.codegen.model.DestinationGeneratingParams
import com.ramcosta.composedestinations.codegen.model.Importable
import com.ramcosta.composedestinations.codegen.model.Parameter
import com.ramcosta.composedestinations.codegen.writers.helpers.ImportableHelper

class DestinationContentFunctionWriter(
    private val destination: DestinationGeneratingParams,
    private val navArgs: List<Parameter>,
    private val importableHelper: ImportableHelper,
) {

    fun write(): String = with(destination) {
        val functionCallCode = StringBuilder()

        val (args, needsDependencyContainer) = prepareArguments()
        if (needsDependencyContainer) {
            functionCallCode += "\t\tval dependencyContainer = dependencies\n"
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
                val animatedVisPlaceholder = importableHelper.addAndGetPlaceholder(
                    Importable(
                        ANIMATED_VISIBILITY_SCOPE_SIMPLE_NAME,
                        ANIMATED_VISIBILITY_SCOPE_QUALIFIED_NAME
                    )
                )
                "val animatedVisibilityScope = (this as $animatedVisPlaceholder)\n" +
                        "\t\tanimatedVisibilityScope."
            }

            COLUMN_SCOPE_SIMPLE_NAME -> {
                val columnScopePlaceholder = importableHelper.addAndGetPlaceholder(
                    Importable(
                        COLUMN_SCOPE_SIMPLE_NAME,
                        COLUMN_SCOPE_QUALIFIED_NAME
                    )
                )
                "val columnScope = (this as $columnScopePlaceholder)\n" +
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
        val arg = when (parameter.type.importable.qualifiedName) {
            NAV_CONTROLLER_QUALIFIED_NAME,
            NAV_HOST_CONTROLLER_QUALIFIED_NAME, -> "navController"
            NAV_BACK_STACK_ENTRY_QUALIFIED_NAME -> "navBackStackEntry"
            DESTINATIONS_NAVIGATOR_QUALIFIED_NAME -> "destinationsNavigator"
            RESULT_RECIPIENT_QUALIFIED_NAME -> {
                val placeHolder = importableHelper.addAndGetPlaceholder(
                    Importable(
                        "resultRecipient",
                        "$CORE_PACKAGE_NAME.scope.resultRecipient"
                    )
                )
                "$placeHolder()"
            }
            RESULT_BACK_NAVIGATOR_QUALIFIED_NAME -> {
                val placeHolder = importableHelper.addAndGetPlaceholder(
                    Importable(
                        "resultBackNavigator",
                        "$CORE_PACKAGE_NAME.scope.resultBackNavigator"
                    )
                )
                "$placeHolder()"
            }
            destination.navArgsDelegateType?.type?.qualifiedName -> {
                "navArgs"
            }
            else -> {
                when {
                    navArgs.contains(parameter) -> {
                        parameter.name //this is resolved by argsFrom before the function
                    }

                    !parameter.hasDefault -> {
                        needsDependencyContainer = true

                        val requirePlaceholder = importableHelper.addAndGetPlaceholder(
                            Importable(
                                "require",
                                "com.ramcosta.composedestinations.navigation.require"
                            )
                        )

                        if (parameter.isMarkedNavHostParam) {
                            "dependencyContainer.$requirePlaceholder(true)"
                        } else {
                            "dependencyContainer.$requirePlaceholder()"
                        }
                    }

                    else -> null
                }
            }
        }

        return arg to needsDependencyContainer
    }
}