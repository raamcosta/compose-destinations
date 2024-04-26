package com.ramcosta.composedestinations.codegen.writers.sub

import com.ramcosta.composedestinations.codegen.commons.ANIMATED_VISIBILITY_SCOPE_QUALIFIED_NAME
import com.ramcosta.composedestinations.codegen.commons.ANIMATED_VISIBILITY_SCOPE_SIMPLE_NAME
import com.ramcosta.composedestinations.codegen.commons.COLUMN_SCOPE_QUALIFIED_NAME
import com.ramcosta.composedestinations.codegen.commons.COLUMN_SCOPE_SIMPLE_NAME
import com.ramcosta.composedestinations.codegen.commons.CORE_PACKAGE_NAME
import com.ramcosta.composedestinations.codegen.commons.DESTINATIONS_NAVIGATOR_QUALIFIED_NAME
import com.ramcosta.composedestinations.codegen.commons.NAV_BACK_STACK_ENTRY_QUALIFIED_NAME
import com.ramcosta.composedestinations.codegen.commons.NAV_CONTROLLER_QUALIFIED_NAME
import com.ramcosta.composedestinations.codegen.commons.NAV_HOST_CONTROLLER_QUALIFIED_NAME
import com.ramcosta.composedestinations.codegen.commons.RESULT_BACK_NAVIGATOR_QUALIFIED_NAME
import com.ramcosta.composedestinations.codegen.commons.RESULT_RECIPIENT_QUALIFIED_NAME
import com.ramcosta.composedestinations.codegen.commons.plusAssign
import com.ramcosta.composedestinations.codegen.model.DestinationGeneratingParams
import com.ramcosta.composedestinations.codegen.model.Importable
import com.ramcosta.composedestinations.codegen.model.Parameter
import com.ramcosta.composedestinations.codegen.writers.helpers.ImportableHelper

class DestinationContentFunctionWriter(
    private val destination: DestinationGeneratingParams,
    private val navArgs: List<Parameter>,
    private val importableHelper: ImportableHelper,
) {

    private val requirePlaceholder = importableHelper.addAndGetPlaceholder(
        Importable(
            "require",
            "com.ramcosta.composedestinations.navigation.require"
        )
    )

    private val listOfPreSupportedTypes = mapOf(
        ANIMATED_VISIBILITY_SCOPE_QUALIFIED_NAME to { "(this as $ANIMATED_VISIBILITY_SCOPE_SIMPLE_NAME)" },
        NAV_CONTROLLER_QUALIFIED_NAME to { "navController" },
        NAV_HOST_CONTROLLER_QUALIFIED_NAME to { "navController" },
        NAV_BACK_STACK_ENTRY_QUALIFIED_NAME to { "navBackStackEntry" },
        DESTINATIONS_NAVIGATOR_QUALIFIED_NAME to { "destinationsNavigator" },
        RESULT_RECIPIENT_QUALIFIED_NAME to {
            val placeHolder = importableHelper.addAndGetPlaceholder(
                Importable(
                    "resultRecipient",
                    "$CORE_PACKAGE_NAME.scope.resultRecipient"
                )
            )
            "$placeHolder()"
        },
        RESULT_BACK_NAVIGATOR_QUALIFIED_NAME to {
            val placeHolder = importableHelper.addAndGetPlaceholder(
                Importable(
                    "resultBackNavigator",
                    "$CORE_PACKAGE_NAME.scope.resultBackNavigator"
                )
            )
            "$placeHolder()"
        },
    )

    fun write(): String = with(destination) {
        val functionCallCode = StringBuilder()

        val (args, needsDependencyContainer) = prepareArguments()
        val (receiverCode, receiverNeedsDependencyContainer) = prepareReceiver()
        if (needsDependencyContainer || receiverNeedsDependencyContainer) {
            functionCallCode += "\t\tval dependencyContainer = buildDependencies()\n"
        }

        if (navArgs.isNotEmpty() && destination.destinationNavArgsClass == null) {
            functionCallCode += "\t\tval (${argNamesInLine()}) = navArgs\n"
        }

        functionCallCode += wrappingPrefix()

        val composableCall = "\t\t$receiverCode${composableName}($args)"

        functionCallCode += if (composableWrappers.isEmpty()) composableCall
        else "\t" + composableCall.replace("\n", "\n\t")

        functionCallCode += wrappingSuffix()

        return functionCallCode.toString()
    }

    private fun DestinationGeneratingParams.wrappingPrefix(): String {
        val wrappingPrefix = when {
            composableWrappers.size == 1 -> {
                val wrapPlaceholder = importableHelper.addAndGetPlaceholder(
                    Importable("Wrap", "com.ramcosta.composedestinations.wrapper.Wrap")
                )
                "\t\t$wrapPlaceholder(${importableHelper.addAndGetPlaceholder(composableWrappers.first())}) {\n"
            }

            composableWrappers.isNotEmpty() -> {
                val wrapPlaceholder = importableHelper.addAndGetPlaceholder(
                    Importable("Wrap", "com.ramcosta.composedestinations.wrapper.Wrap")
                )
                "\t\t$wrapPlaceholder(${composableWrappers.joinToString(", ") { importableHelper.addAndGetPlaceholder(it) }}) {\n"
            }

            else -> ""
        }
        return wrappingPrefix
    }

    private fun DestinationGeneratingParams.wrappingSuffix(): String {
        return if (composableWrappers.isNotEmpty()) {
            "\n\t\t}"
        } else {
            ""
        }
    }

    private fun argNamesInLine(): String {
        return navArgs.joinToString(", ") { it.name }
    }

    private fun prepareReceiver(): Pair<String, Boolean> {
        val receiverType = destination.composableReceiverType
        return when (receiverType?.importable?.qualifiedName) {
            null -> "" to false
            ANIMATED_VISIBILITY_SCOPE_QUALIFIED_NAME -> {
                val animatedVisPlaceholder = importableHelper.addAndGetPlaceholder(
                    Importable(
                        ANIMATED_VISIBILITY_SCOPE_SIMPLE_NAME,
                        ANIMATED_VISIBILITY_SCOPE_QUALIFIED_NAME
                    )
                )
                "val animatedVisibilityScope = (this as $animatedVisPlaceholder)\n" +
                        "\t\tanimatedVisibilityScope." to false
            }

            COLUMN_SCOPE_QUALIFIED_NAME -> {
                val columnScopePlaceholder = importableHelper.addAndGetPlaceholder(
                    Importable(
                        COLUMN_SCOPE_SIMPLE_NAME,
                        COLUMN_SCOPE_QUALIFIED_NAME
                    )
                )
                "val columnScope = (this as $columnScopePlaceholder)\n" +
                        "\t\tcolumnScope." to false
            }

            in listOfPreSupportedTypes.keys -> {
                listOfPreSupportedTypes[receiverType.importable.qualifiedName]!!.invoke() + "." to false
            }

            else -> {
                val receiverTypePlaceHolder = importableHelper.addAndGetPlaceholder(receiverType.importable)
                "dependencyContainer.$requirePlaceholder<$receiverTypePlaceHolder>()." to true
            }
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
        val arg = when (val typeQualifiedName = parameter.type.importable.qualifiedName) {
            in listOfPreSupportedTypes.keys -> listOfPreSupportedTypes[typeQualifiedName]!!.invoke()
            destination.destinationNavArgsClass?.type?.qualifiedName -> {
                "navArgs"
            }
            else -> {
                when {
                    navArgs.contains(parameter) -> {
                        parameter.name //this is resolved by argsFrom before the function
                    }

                    !parameter.hasDefault -> {
                        needsDependencyContainer = true

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