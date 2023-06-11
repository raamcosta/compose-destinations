package com.ramcosta.composedestinations.codegen.writers.helpers

import com.ramcosta.composedestinations.codegen.codeGenBasePackageName
import com.ramcosta.composedestinations.codegen.commons.GeneratedExceptions
import com.ramcosta.composedestinations.codegen.commons.IllegalDestinationsSetup
import com.ramcosta.composedestinations.codegen.commons.coreTypes
import com.ramcosta.composedestinations.codegen.commons.isCustomTypeNavArg
import com.ramcosta.composedestinations.codegen.model.CustomNavType
import com.ramcosta.composedestinations.codegen.model.Importable
import com.ramcosta.composedestinations.codegen.model.Parameter
import com.ramcosta.composedestinations.codegen.model.Type
import com.ramcosta.composedestinations.codegen.model.TypeInfo

class NavArgResolver(
    val customNavTypeByType: Map<Type, CustomNavType>,
    private val importableHelper: ImportableHelper
) {

    fun resolve(
        errorLocationPrefix: String,
        parameter: Parameter
    ) = internalResolve(
        argGetter = parameter.type.toNavBackStackEntryArgGetter(
            errorLocationPrefix,
            parameter.name
        ),
        parameter = parameter,
    )

    fun resolveFromSavedStateHandle(
        errorLocationPrefix: String,
        parameter: Parameter,
    ) = internalResolve(
        argGetter = parameter.type.toSavedStateHandleArgGetter(errorLocationPrefix, parameter.name),
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
