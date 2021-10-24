package com.ramcosta.composedestinations.codegen.writers.sub

import com.ramcosta.composedestinations.codegen.commons.*
import com.ramcosta.composedestinations.codegen.model.*

class DestinationContentFunctionWriter(
    private val destination: Destination,
    private val navArgs: List<Parameter>,
    private val additionalImports: MutableSet<String>,
) {

    fun write(): String {
        val contentFunctionCode = StringBuilder()
        if (destination.parameters.any { it.type.qualifiedName == SCAFFOLD_STATE_QUALIFIED_NAME }) {
            additionalImports.add("androidx.compose.material.ScaffoldState")
            contentFunctionCode += "\t\tval scaffoldState = situationalParameters[ScaffoldState::class.java] as? ScaffoldState ?: ${GeneratedExceptions.SCAFFOLD_STATE_MISSING}"
            contentFunctionCode += "\n"
        }

        val receiver = contentFunctionCode.addComposableCallReceiver()

        contentFunctionCode += "\n"
        contentFunctionCode += "\t\t${callComposableCode(receiver, emptyList())}"

        return contentFunctionCode.toString()
    }

    private fun callComposableCode(receiver: String, argumentsToIgnore: List<String>): String = with(destination) {
        return "$receiver${composableName}(${prepareArguments(argumentsToIgnore)})"
    }

    private fun StringBuilder.addComposableCallReceiver(): String {
        return when (destination.composableReceiverSimpleName) {
            ANIMATED_VISIBILITY_SCOPE_SIMPLE_NAME -> {
                additionalImports.add(ANIMATED_VISIBILITY_SCOPE_QUALIFIED_NAME)
                this += "\t\tval animatedVisibilityScope = situationalParameters[$ANIMATED_VISIBILITY_SCOPE_SIMPLE_NAME::class.java] as? $ANIMATED_VISIBILITY_SCOPE_SIMPLE_NAME ?: ${GeneratedExceptions.MISSING_VISIBILITY_SCOPE}\n"

                "animatedVisibilityScope."
            }
            COLUMN_SCOPE_SIMPLE_NAME -> {
                additionalImports.add("androidx.compose.foundation.layout.$COLUMN_SCOPE_SIMPLE_NAME")
                this += "\t\tval columnScope = situationalParameters[$COLUMN_SCOPE_SIMPLE_NAME::class.java] as? $COLUMN_SCOPE_SIMPLE_NAME ?: ${GeneratedExceptions.MISSING_COLUMN_SCOPE}\n"

                "columnScope."
            }
            else -> {
                ""
            }
        }
    }

    private fun prepareArguments(argumentsToIgnore: List<String>): String = with(destination) {
        var argsCode = ""

        val filteredParams = parameters.filter { !argumentsToIgnore.contains(it.name) }

        filteredParams.forEachIndexed { i, it ->

            val argumentResolver = resolveArgumentForTypeAndName(it)

            if (argumentResolver != null) {
                if (i != 0) argsCode += ", "

                argsCode += "\n\t\t\t${it.name} = $argumentResolver"

            } else if (!it.hasDefault) {
                throw IllegalDestinationsSetup("Composable: $composableName - Unresolvable argument type without default value: $it")
            }

            if (i == filteredParams.lastIndex) argsCode += "\n\t\t"
        }

        return argsCode
    }

    private fun resolveArgumentForTypeAndName(parameter: Parameter): String? {
        return when (parameter.type.qualifiedName) {
            NAV_CONTROLLER_QUALIFIED_NAME,
            NAV_HOST_CONTROLLER_QUALIFIED_NAME, -> "navController"
            DESTINATIONS_NAVIGATOR_QUALIFIED_NAME -> "$CORE_NAV_DESTINATIONS_NAVIGATION(navController, navBackStackEntry)"
            NAV_BACK_STACK_ENTRY_QUALIFIED_NAME -> "navBackStackEntry"
            SCAFFOLD_STATE_QUALIFIED_NAME -> "scaffoldState"
            else -> {
                if (navArgs.contains(parameter)) {
                    "navBackStackEntry.arguments?.${parameter.type.toNavBackStackEntryArgGetter(parameter.name)}${defaultCodeIfArgNotPresent(parameter)}"
                } else null
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