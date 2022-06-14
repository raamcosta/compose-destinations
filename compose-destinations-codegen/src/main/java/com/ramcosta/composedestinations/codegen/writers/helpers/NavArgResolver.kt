package com.ramcosta.composedestinations.codegen.writers.helpers

import com.ramcosta.composedestinations.codegen.codeGenBasePackageName
import com.ramcosta.composedestinations.codegen.commons.*
import com.ramcosta.composedestinations.codegen.model.*

class NavArgResolver(
    private val customNavTypeByType: Map<Type, CustomNavType>,
    private val importableHelper: ImportableHelper
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

    fun customNavTypeCode(type: TypeInfo): String {
        val navTypeName = customNavTypeByType[type.value]!!.name
        return importableHelper.addAndGetPlaceholder(
            Importable(
                navTypeName,
                "$codeGenBasePackageName.navtype.$navTypeName"
            )
        )
    }

    private fun internalResolve(
        argGetter: String,
        parameter: Parameter,
    ): String {
        return argGetter + defaultCodeIfArgNotPresent(parameter)
    }

    private fun TypeInfo.toSavedStateHandleArgGetter(
        destination: DestinationGeneratingParams,
        argName: String,
    ): String {
        return when {
            value in coreTypes.keys -> "${coreTypes[value]!!.simpleName}.get(savedStateHandle, \"$argName\")"
            isCustomTypeNavArg() -> "${customNavTypeCode(this)}.get(savedStateHandle, \"$argName\")"
            else -> throw IllegalDestinationsSetup("Composable '${destination.composableName}': Unknown type $importable.qualifiedName")
        }
    }

    private fun TypeInfo.toNavBackStackEntryArgGetter(
        destination: DestinationGeneratingParams,
        argName: String,
    ): String {
        return when {
            value in coreTypes.keys -> "${coreTypes[value]!!.simpleName}.get(navBackStackEntry, \"$argName\")"
            isCustomTypeNavArg() -> "${customNavTypeCode(this)}.get(navBackStackEntry, \"$argName\")"
            else -> throw IllegalDestinationsSetup("Composable '${destination.composableName}': Unknown type ${importable.qualifiedName}")
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
