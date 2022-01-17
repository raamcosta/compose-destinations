package com.ramcosta.composedestinations.codegen.writers.sub

import com.ramcosta.composedestinations.codegen.commons.GeneratedExceptions
import com.ramcosta.composedestinations.codegen.commons.IllegalDestinationsSetup
import com.ramcosta.composedestinations.codegen.model.DestinationGeneratingParams
import com.ramcosta.composedestinations.codegen.model.Parameter
import com.ramcosta.composedestinations.codegen.model.Type

class NavArgResolver {

    fun resolve(
        destination: DestinationGeneratingParams,
        additionalImports: MutableSet<String>,
        parameter: Parameter
    ) = internalResolve(
        argGetter = "navBackStackEntry.arguments?.${parameter.type.toNavBackStackEntryArgGetter(destination, parameter.name)}",
        additionalImports = additionalImports,
        parameter = parameter,
    )

    fun resolveFromSavedStateHandle(
        destination: DestinationGeneratingParams,
        additionalImports: MutableSet<String>,
        parameter: Parameter,
    ) = internalResolve(
        argGetter = "savedStateHandle.${parameter.type.toSavedStateHandleArgGetter(destination, parameter.name)}",
        additionalImports = additionalImports,
        parameter = parameter,
    )

    private fun internalResolve(
        argGetter: String,
        additionalImports: MutableSet<String>,
        parameter: Parameter,
    ): String {
        val defaultCodeIfArgNotPresent = defaultCodeIfArgNotPresent(additionalImports, parameter)

        return if (parameter.type.isEnum) {
            val stringToArg = "${parameter.type.classType.simpleName}.valueOf(it)"
            buildNavArgForStringifiedEnumTypes(argGetter, stringToArg, defaultCodeIfArgNotPresent)
        } else {
            argGetter + defaultCodeIfArgNotPresent
        }
    }

    private fun buildNavArgForStringifiedEnumTypes(
        argGetter: String,
        stringToArgCode: String,
        defaultCodeIfArgNotPresent: String,
    ): String {
        return "$argGetter?.let { $stringToArgCode }$defaultCodeIfArgNotPresent"
    }

    private fun Type.toSavedStateHandleArgGetter(
        destination: DestinationGeneratingParams,
        argName: String,
    ): String {
        return when (classType.qualifiedName) {
            String::class.qualifiedName -> "get<String>(\"$argName\")"
            Int::class.qualifiedName -> "get<Int>(\"$argName\")"
            Float::class.qualifiedName -> "get<Float>(\"$argName\")"
            Long::class.qualifiedName -> "get<Long>(\"$argName\")"
            Boolean::class.qualifiedName -> "get<Boolean>(\"$argName\")"
            else -> {
                return when {
                    isEnum -> {
                        "get<String>(\"$argName\")"
                    }
                    isParcelable || isSerializable -> {
                        "get(\"$argName\")"
                    }
                    else -> throw IllegalDestinationsSetup("Composable '${destination.composableName}': Unknown type $classType.qualifiedName")
                }

            }
        }
    }

    private fun Type.toNavBackStackEntryArgGetter(
        destination: DestinationGeneratingParams,
        argName: String,
    ): String {
        return when (classType.qualifiedName) {
            String::class.qualifiedName -> "getString(\"$argName\")"
            Int::class.qualifiedName -> "getInt(\"$argName\")"
            Float::class.qualifiedName -> "getFloat(\"$argName\")"
            Long::class.qualifiedName -> "getLong(\"$argName\")"
            Boolean::class.qualifiedName -> "getBoolean(\"$argName\")"
            else -> {
                return when {
                    isParcelable -> {
                        "getParcelable(\"$argName\")"
                    }
                    isEnum -> {
                        "getString(\"$argName\")"
                    }
                    isSerializable -> {
                        "getSerializable(\"$argName\") as? ${this.classType.simpleName}?"
                    }
                    else -> throw IllegalDestinationsSetup("Composable '${destination.composableName}': Unknown type ${classType.qualifiedName}")
                }
            }
        }
    }

    private fun defaultCodeIfArgNotPresent(
        additionalImports: MutableSet<String>,
        parameter: Parameter,
    ): String = parameter.defaultValue.let { defaultValue ->
        if (defaultValue == null) {
            return if (parameter.isNullable) {
                ""
            } else {
                " ?: ${GeneratedExceptions.missingMandatoryArgument(parameter.name)}"
            }
        }

        defaultValue.imports.forEach { additionalImports.add(it) }

        return if (defaultValue.code == "null") {
            ""
        } else " ?: ${defaultValue.code}"
    }
}