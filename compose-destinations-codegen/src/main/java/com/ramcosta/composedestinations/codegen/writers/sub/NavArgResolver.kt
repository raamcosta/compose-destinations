package com.ramcosta.composedestinations.codegen.writers.sub

import com.ramcosta.composedestinations.codegen.commons.GeneratedExceptions
import com.ramcosta.composedestinations.codegen.commons.IllegalDestinationsSetup
import com.ramcosta.composedestinations.codegen.commons.toNavTypeCodeOrNull
import com.ramcosta.composedestinations.codegen.model.Destination
import com.ramcosta.composedestinations.codegen.model.Parameter
import com.ramcosta.composedestinations.codegen.model.Type

object NavArgResolver {

    fun resolve(
        destination: Destination,
        additionalImports: MutableSet<String>,
        parameter: Parameter,
        isComposable: Boolean = false,
    ) = internalResolve(
        argGetter = "navBackStackEntry.arguments?.${parameter.type.toNavBackStackEntryArgGetter(destination, parameter.name)}",
        additionalImports = additionalImports,
        parameter = parameter,
        isComposable = isComposable
    )

    fun resolveFromSavedStateHandle(
        destination: Destination,
        additionalImports: MutableSet<String>,
        parameter: Parameter,
    ) = internalResolve(
        argGetter = "savedStateHandle.${parameter.type.toSavedStateHandleArgGetter(destination, parameter.name)}",
        additionalImports = additionalImports,
        parameter = parameter,
        isComposable = false
    )

    private fun internalResolve(
        argGetter: String,
        additionalImports: MutableSet<String>,
        parameter: Parameter,
        isComposable: Boolean
    ): String {
        val defaultCodeIfArgNotPresent = defaultCodeIfArgNotPresent(additionalImports, parameter)

        return when {
            parameter.type.isEnum -> {
                val stringToArg = "${parameter.type.simpleName}.valueOf(it)"
                buildNavArgForStringifiedComplexOrEnumTypes(argGetter, stringToArg, defaultCodeIfArgNotPresent, isComposable)
            }
            parameter.type.isParcelable -> {
                if (isComposable) additionalImports.add("androidx.compose.runtime.remember")

                val stringToArg = "Base64Utils.base64ToParcelable(it, ${parameter.type.simpleName}.CREATOR)"
                buildNavArgForStringifiedComplexOrEnumTypes(argGetter, stringToArg, defaultCodeIfArgNotPresent, isComposable)
            }
            parameter.type.isSerializable && parameter.type.toNavTypeCodeOrNull() == null -> {
                if (isComposable) additionalImports.add("androidx.compose.runtime.remember")

                val stringToArg = "Base64Utils.base64ToSerializable(it)"
                buildNavArgForStringifiedComplexOrEnumTypes(argGetter, stringToArg, defaultCodeIfArgNotPresent, isComposable)
            }
            else -> argGetter + defaultCodeIfArgNotPresent
        }
    }

    private fun buildNavArgForStringifiedComplexOrEnumTypes(
        argGetter: String,
        stringToArgCode: String,
        defaultCodeIfArgNotPresent: String,
        isComposable: Boolean = false,
    ): String {
        val rememberPrefix = if (isComposable) "remember { " else ""
        val rememberSuffix = if (isComposable) " }" else ""

        return "$argGetter?.let { $rememberPrefix$stringToArgCode$rememberSuffix }$defaultCodeIfArgNotPresent"
    }

    private fun Type.toSavedStateHandleArgGetter(
        destination: Destination,
        argName: String,
    ): String {
        return when (qualifiedName) {
            String::class.qualifiedName -> "get<String>(\"$argName\")"
            Int::class.qualifiedName -> "get<Int>(\"$argName\")"
            Float::class.qualifiedName -> "get<Float>(\"$argName\")"
            Long::class.qualifiedName -> "get<Long>(\"$argName\")"
            Boolean::class.qualifiedName -> "get<Boolean>(\"$argName\")"
            else -> {
                if (isEnum || isParcelable || isSerializable) {
                    return "get<String>(\"$argName\")"
                }

                throw IllegalDestinationsSetup("Composable '${destination.composableName}': Unknown type $qualifiedName")
            }
        }
    }

    private fun Type.toNavBackStackEntryArgGetter(
        destination: Destination,
        argName: String,
    ): String {
        return when (qualifiedName) {
            String::class.qualifiedName -> "getString(\"$argName\")"
            Int::class.qualifiedName -> "getInt(\"$argName\")"
            Float::class.qualifiedName -> "getFloat(\"$argName\")"
            Long::class.qualifiedName -> "getLong(\"$argName\")"
            Boolean::class.qualifiedName -> "getBoolean(\"$argName\")"
            else -> {
                if (isEnum || isParcelable || isSerializable) {
                    return "getString(\"$argName\")"
                }

                throw IllegalDestinationsSetup("Composable '${destination.composableName}': Unknown type $qualifiedName")
            }
        }
    }

    private fun defaultCodeIfArgNotPresent(
        additionalImports: MutableSet<String>,
        parameter: Parameter,
    ): String {
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