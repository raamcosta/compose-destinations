package com.ramcosta.composedestinations.codegen.commons

import com.ramcosta.composedestinations.codegen.facades.Logger
import com.ramcosta.composedestinations.codegen.model.DestinationGeneratingParams
import com.ramcosta.composedestinations.codegen.model.DestinationGeneratingParamsWithNavArgs
import com.ramcosta.composedestinations.codegen.model.Parameter
import com.ramcosta.composedestinations.codegen.model.TypeInfo

class DestinationWithNavArgsMapper {

    fun map(destinations: List<DestinationGeneratingParams>): List<DestinationGeneratingParamsWithNavArgs> {
        return destinations.map {
            DestinationGeneratingParamsWithNavArgs(
                it.getNavArgs(),
                it
            )
        }
    }

    private fun DestinationGeneratingParams.getNavArgs(): List<Parameter> {
        val navArgsDelegateTypeLocal = destinationNavArgsClass
        return if (navArgsDelegateTypeLocal == null) {
            parameters.filter { it.isNavArg() }
        } else {
            val nonNavArg = navArgsDelegateTypeLocal.parameters.firstOrNull { !it.isNavArg() }
            if (nonNavArg != null) {
                if (!nonNavArg.type.isCoreOrCustomNavArgType() &&
                    nonNavArg.type.valueClassInnerInfo != null && // is value class
                    !nonNavArg.type.isValueClassOfValidInnerType()) {

                    throw IllegalDestinationsSetup("Composable '${composableName}': " +
                            "'$DESTINATION_ANNOTATION_NAV_ARGS_DELEGATE_ARGUMENT' cannot have arguments that are not navigation types. (check argument '${nonNavArg.name}')\n" +
                            "HINT: value classes are only valid navigation arguments if they have a public constructor with a public non nullable field which is itself of a navigation type.")
                }

                throw IllegalDestinationsSetup("Composable '${composableName}': " +
                        "'$DESTINATION_ANNOTATION_NAV_ARGS_DELEGATE_ARGUMENT' cannot have arguments that are not navigation types. (check argument '${nonNavArg.name}')")
            }

            val navArgInFuncParams = parameters.firstOrNull { it.isNavArg() && it.type.value.importable != navArgsDelegateType?.type }
            if (navArgInFuncParams != null) {
                throw IllegalDestinationsSetup("Composable '${composableName}': annotated " +
                        "function cannot define arguments of navigation type if using a '$DESTINATION_ANNOTATION_NAV_ARGS_DELEGATE_ARGUMENT' class. (check argument '${navArgInFuncParams.name})'")
            }

            navArgsDelegateTypeLocal.parameters
        }
    }

    private fun Parameter.isNavArg(): Boolean {
        if (isMarkedNavHostParam) {
            if (!type.isNavArgType()) {
                Logger.instance.info("Parameter ${this.name}: annotation @NavHostParam is redundant since it" +
                        " is not a navigation argument type anyway.")
            }
            return false
        }

        return type.isNavArgType()
    }

    private fun TypeInfo.isNavArgType(): Boolean {
        if (isCoreOrCustomNavArgType()) {
            return true
        }

        return isValueClassOfValidInnerType()
    }

    private fun TypeInfo.isValueClassOfValidInnerType(): Boolean {
        if (valueClassInnerInfo != null &&
            valueClassInnerInfo.isConstructorPublic &&
            valueClassInnerInfo.publicNonNullableField != null
        ) {
            return valueClassInnerInfo.typeInfo.isCoreOrCustomNavArgType()
        }

        return false
    }

    private fun TypeInfo.isCoreOrCustomNavArgType(): Boolean {
        if (isCoreType()) {
            return true
        }

        if (isCustomTypeNavArg()) {
            return true
        }

        return false
    }
}
