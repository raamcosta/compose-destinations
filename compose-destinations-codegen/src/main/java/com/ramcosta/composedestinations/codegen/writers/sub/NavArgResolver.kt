package com.ramcosta.composedestinations.codegen.writers.sub

import com.ramcosta.composedestinations.codegen.commons.*
import com.ramcosta.composedestinations.codegen.model.*

class NavArgResolver(
    private val customNavTypeByType: Map<ClassType, CustomNavType>
) {

    fun resolve(
        destination: DestinationGeneratingParams,
        parameter: Parameter
    ) = internalResolve(
        argGetter = parameter.type.toNavBackStackEntryArgGetter(
            destination,
            parameter.name
        ),
        parameter = parameter,
    )

    fun resolveFromSavedStateHandle(
        destination: DestinationGeneratingParams,
        parameter: Parameter,
    ) = internalResolve(
        argGetter = parameter.type.toSavedStateHandleArgGetter(destination, parameter.name),
        parameter = parameter,
    )

    private fun internalResolve(
        argGetter: String,
        parameter: Parameter,
    ): String {
        val defaultCodeIfArgNotPresent = defaultCodeIfArgNotPresent(parameter)

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
            String::class.qualifiedName -> "${CORE_STRING_NAV_TYPE.simpleName}.get(savedStateHandle, \"$argName\")"
            Int::class.qualifiedName -> "${CORE_INT_NAV_TYPE.simpleName}.get(savedStateHandle, \"$argName\")"
            Float::class.qualifiedName -> "${CORE_FLOAT_NAV_TYPE.simpleName}.get(savedStateHandle, \"$argName\")"
            Long::class.qualifiedName -> "${CORE_LONG_NAV_TYPE.simpleName}.get(savedStateHandle, \"$argName\")"
            Boolean::class.qualifiedName -> "${CORE_BOOLEAN_NAV_TYPE.simpleName}.get(savedStateHandle, \"$argName\")"
            else -> {
                return when {
                    isEnum -> {
                        "savedStateHandle.get<String>(\"$argName\")"
                    }
                    isParcelable || isSerializable || hasCustomTypeSerializer || isKtxSerializable -> {
                        val navTypeName = customNavTypeByType[this.classType]!!.name
                        "$navTypeName.get(savedStateHandle, \"$argName\")"
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
            String::class.qualifiedName -> "${CORE_STRING_NAV_TYPE.simpleName}.get(navBackStackEntry, \"$argName\")"
            Int::class.qualifiedName -> "${CORE_INT_NAV_TYPE.simpleName}.get(navBackStackEntry, \"$argName\")"
            Float::class.qualifiedName -> "${CORE_FLOAT_NAV_TYPE.simpleName}.get(navBackStackEntry, \"$argName\")"
            Long::class.qualifiedName -> "${CORE_LONG_NAV_TYPE.simpleName}.get(navBackStackEntry, \"$argName\")"
            Boolean::class.qualifiedName -> "${CORE_BOOLEAN_NAV_TYPE.simpleName}.get(navBackStackEntry, \"$argName\")"
            else -> {
                return when {
                    isEnum -> {
                        "navBackStackEntry.arguments?.getString(\"$argName\")"
                    }
                    isParcelable || isSerializable || hasCustomTypeSerializer || isKtxSerializable -> {
                        val navTypeName = customNavTypeByType[this.classType]!!.name
                        "$navTypeName.get(navBackStackEntry, \"$argName\")"
                    }
                    else -> throw IllegalDestinationsSetup("Composable '${destination.composableName}': Unknown type ${classType.qualifiedName}")
                }
            }
        }
    }

    private fun defaultCodeIfArgNotPresent(
        parameter: Parameter,
    ): String = when {
        parameter.type.isNullable -> ""
        parameter.isMandatory -> " ?: ${GeneratedExceptions.missingMandatoryArgument(parameter.name)}"
        else -> " ?: ${GeneratedExceptions.nonMandatoryNonNullableMissingArgument(parameter.name)}"
    }
}
