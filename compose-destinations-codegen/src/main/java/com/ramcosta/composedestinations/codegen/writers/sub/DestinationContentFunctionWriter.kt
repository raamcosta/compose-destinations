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
                "val animatedVisibilityScope = container.get<$ANIMATED_VISIBILITY_SCOPE_SIMPLE_NAME>()\n" +
                        "\t\tanimatedVisibilityScope."
            }

            COLUMN_SCOPE_SIMPLE_NAME -> {
                additionalImports.add(COLUMN_SCOPE_QUALIFIED_NAME)
                "val columnScope = container.get<$COLUMN_SCOPE_SIMPLE_NAME>()\n" +
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
            destination.navArgsDelegateType?.qualifiedName -> "argsFrom(navBackStackEntry)"
            else -> {
                if (navArgs.contains(parameter)) {
                    resolveNavArg(destination, additionalImports, parameter)

                } else if (!parameter.hasDefault) {
                    additionalImports.add(parameter.type.qualifiedName)
                    "container.get<${parameter.type.simpleName}>()"
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

    companion object {

        fun resolveNavArg(
            destination: Destination,
            additionalImports: MutableSet<String>,
            parameter: Parameter,
        ): String {
            return "navBackStackEntry.arguments?." +
                    parameter.type.toNavBackStackEntryArgGetter(destination, parameter.name) +
                    defaultCodeIfArgNotPresent(additionalImports, parameter)
        }

        fun resolveNavArgFromSavedStateHandle(
            destination: Destination,
            additionalImports: MutableSet<String>,
            parameter: Parameter,
        ): String {
            return "savedStateHandle." +
                    parameter.type.toSavedStateHandleArgGetter(destination, parameter.name) +
                    defaultCodeIfArgNotPresent(additionalImports, parameter)
        }

        private fun Type.toSavedStateHandleArgGetter(destination: Destination, argName: String): String {
            return when (qualifiedName) {
                String::class.qualifiedName -> "get<String>(\"$argName\")"
                Int::class.qualifiedName -> "get<Int>(\"$argName\")"
                Float::class.qualifiedName -> "get<Float>(\"$argName\")"
                Long::class.qualifiedName -> "get<Long>(\"$argName\")"
                Boolean::class.qualifiedName -> "get<Boolean>(\"$argName\")"
                else -> throw IllegalDestinationsSetup("Composable '${destination.composableName}': Unknown type $qualifiedName")
            }
        }

        private fun Type.toNavBackStackEntryArgGetter(destination: Destination, argName: String): String {
            return when (qualifiedName) {
                String::class.qualifiedName -> "getString(\"$argName\")"
                Int::class.qualifiedName -> "getInt(\"$argName\")"
                Float::class.qualifiedName -> "getFloat(\"$argName\")"
                Long::class.qualifiedName -> "getLong(\"$argName\")"
                Boolean::class.qualifiedName -> "getBoolean(\"$argName\")"
                else -> throw IllegalDestinationsSetup("Composable '${destination.composableName}': Unknown type $qualifiedName")
            }
        }

        private fun defaultCodeIfArgNotPresent(additionalImports: MutableSet<String>, parameter: Parameter): String {
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

    }
}