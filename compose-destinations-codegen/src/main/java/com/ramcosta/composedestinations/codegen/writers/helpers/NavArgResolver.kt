package com.ramcosta.composedestinations.codegen.writers.helpers

import com.ramcosta.composedestinations.codegen.commons.GeneratedExceptions
import com.ramcosta.composedestinations.codegen.commons.IllegalDestinationsSetup
import com.ramcosta.composedestinations.codegen.commons.coreTypes
import com.ramcosta.composedestinations.codegen.commons.isCustomTypeNavArg
import com.ramcosta.composedestinations.codegen.model.CustomNavType
import com.ramcosta.composedestinations.codegen.model.Parameter
import com.ramcosta.composedestinations.codegen.model.Type
import com.ramcosta.composedestinations.codegen.model.TypeInfo

class NavArgResolver(
    val customNavTypeByType: Map<Type, CustomNavType>,
    private val importableHelper: ImportableHelper
) {

    fun resolve(
        errorLocationPrefix: String,
        parameter: Parameter,
        defaultIfArgNotPresent: ((Parameter) -> String)? = null
    ) = internalResolve(
        argGetter = parameter.type.toNavBackStackEntryArgGetter(
            errorLocationPrefix,
            parameter.name
        ),
        parameter = parameter,
        defaultIfArgNotPresent = defaultIfArgNotPresent,
    )

    fun resolveFromSavedStateHandle(
        errorLocationPrefix: String,
        parameter: Parameter,
        defaultIfArgNotPresent: ((Parameter) -> String)? = null
    ) = internalResolve(
        argGetter = parameter.type.toSavedStateHandleArgGetter(errorLocationPrefix, parameter.name),
        parameter = parameter,
        defaultIfArgNotPresent = defaultIfArgNotPresent,
    )

    fun resolveToSavedStateHandle(parameter: Parameter) = parameter.type.toSavedStateHandleArgPutter(parameter.name)

    fun customNavTypeCode(type: TypeInfo): String {
        val importable = customNavTypeByType[type.value]!!.importable
        return importableHelper.addAndGetPlaceholder(importable)
    }

    private fun internalResolve(
        argGetter: String,
        parameter: Parameter,
        defaultIfArgNotPresent: ((Parameter) -> String)?
    ): String {
        return argGetter + if (defaultIfArgNotPresent != null) defaultIfArgNotPresent(parameter) else defaultCodeIfArgNotPresent(parameter)
    }

    private fun TypeInfo.toSavedStateHandleArgGetter(
        errorLocationPrefix: String,
        argName: String,
    ): String {
        return when {
            value in coreTypes.keys -> "${coreTypes[value]!!.simpleName}.get(savedStateHandle, \"$argName\")"
            isCustomTypeNavArg() -> "${customNavTypeCode(this)}.get(savedStateHandle, \"$argName\")"
            valueClassInnerInfo != null -> {
                valueClassInnerInfo.typeInfo.toSavedStateHandleArgGetter(errorLocationPrefix, argName) +
                        "?.let { ${importableHelper.addAndGetPlaceholder(importable)}(it) }"
            }
            else -> throw IllegalDestinationsSetup("$errorLocationPrefix': Unknown type $importable.qualifiedName")
        }
    }

    private fun TypeInfo.toSavedStateHandleArgPutter(
        argName: String,
        valueClassSuffix: String = ""
    ): String {
        return when {
            value in coreTypes.keys -> "${coreTypes[value]!!.simpleName}.put(handle, \"$argName\", $argName$valueClassSuffix)"
            isCustomTypeNavArg() -> "${customNavTypeCode(this)}.put(handle, \"$argName\", $argName$valueClassSuffix)"
            valueClassInnerInfo != null -> {
                valueClassInnerInfo.typeInfo.toSavedStateHandleArgPutter(argName, ".${valueClassInnerInfo.publicNonNullableField!!}")
            }
            else -> throw IllegalDestinationsSetup("Unknown type $importable.qualifiedName")
        }
    }

    private fun TypeInfo.toNavBackStackEntryArgGetter(
        errorLocationPrefix: String,
        argName: String,
    ): String {
        return when {
            value in coreTypes.keys -> "${coreTypes[value]!!.simpleName}.safeGet(bundle, \"$argName\")"
            isCustomTypeNavArg() -> "${customNavTypeCode(this)}.safeGet(bundle, \"$argName\")"
            valueClassInnerInfo != null -> {
                valueClassInnerInfo.typeInfo.toNavBackStackEntryArgGetter(errorLocationPrefix, argName) +
                        "?.let { ${importableHelper.addAndGetPlaceholder(importable)}(it) }"
            }
            else -> throw IllegalDestinationsSetup("$errorLocationPrefix: Unknown type ${importable.qualifiedName}")
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
