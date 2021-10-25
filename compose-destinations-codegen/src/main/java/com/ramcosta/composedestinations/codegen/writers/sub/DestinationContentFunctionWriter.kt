package com.ramcosta.composedestinations.codegen.writers.sub

import com.ramcosta.composedestinations.codegen.commons.*
import com.ramcosta.composedestinations.codegen.model.*

class DestinationContentFunctionWriter(
    private val destination: Destination,
    private val navArgs: List<Parameter>,
    private val additionalImports: MutableSet<String>,
) {

    fun write(): String = with(destination) {
        val receiver = prepareReceiver()
        return "\t\t$receiver${composableName}(${prepareArguments()})"
    }

    private fun prepareReceiver(): String {
        return when (destination.composableReceiverSimpleName) {
            ANIMATED_VISIBILITY_SCOPE_SIMPLE_NAME -> {
                additionalImports.add(ANIMATED_VISIBILITY_SCOPE_QUALIFIED_NAME)
                "val animatedVisibilityScope = destinationDependencies[$ANIMATED_VISIBILITY_SCOPE_SIMPLE_NAME::class.java] as? $ANIMATED_VISIBILITY_SCOPE_SIMPLE_NAME ?: ${GeneratedExceptions.MISSING_VISIBILITY_SCOPE}\n" +
                        "\t\tanimatedVisibilityScope."
            }

            COLUMN_SCOPE_SIMPLE_NAME -> {
                additionalImports.add("androidx.compose.foundation.layout.$COLUMN_SCOPE_SIMPLE_NAME")
                "val columnScope = destinationDependencies[$COLUMN_SCOPE_SIMPLE_NAME::class.java] as? $COLUMN_SCOPE_SIMPLE_NAME ?: ${GeneratedExceptions.MISSING_COLUMN_SCOPE}\n" +
                        "\t\tcolumnScope."
            }

            else -> ""
        }
    }

    private fun prepareArguments(): String = with(destination) {
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
            else -> {
                if (navArgs.contains(parameter)) {
                    "navBackStackEntry.arguments?.${parameter.type.toNavBackStackEntryArgGetter(parameter.name)}${defaultCodeIfArgNotPresent(parameter)}"
                } else if (!parameter.hasDefault) {
                    additionalImports.add(parameter.type.qualifiedName)
                    "destinationDependencies[${parameter.type.simpleName}::class.java] as? ${parameter.type.simpleName}? ?: ${GeneratedExceptions.missingRequestedArgument(parameter.type.simpleName, destination.composableName)}"
                } else {
                    null
                }
            }
        }
    }

    private fun defaultCodeIfArgNotPresent(parameter: Parameter): String {
        if (parameter.defaultValue == null) {
            return if (parameter.type.isNullable) {
                ""
            } else {
                " ?: ${GeneratedExceptions.missingMandatoryArgument(parameter.name)}"
            }
        }

        parameter.defaultValue.imports.forEach { additionalImports.add(it) }

        return if (parameter.defaultValue.code == "null") {
            ""
        } else " ?: ${parameter.defaultValue.code}"
    }

    private fun Type.toNavBackStackEntryArgGetter(argName: String): String {
        return when (qualifiedName) {
            String::class.qualifiedName -> "getString(\"$argName\")"
            Int::class.qualifiedName -> "getInt(\"$argName\")"
            Float::class.qualifiedName -> "getFloat(\"$argName\")"
            Long::class.qualifiedName -> "getLong(\"$argName\")"
            Boolean::class.qualifiedName -> "getBoolean(\"$argName\")"
            else -> throw IllegalDestinationsSetup("Composable '${destination.composableName}': Unknown type $qualifiedName")
        }
    }
}