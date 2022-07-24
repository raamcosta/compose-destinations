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
        val navArgsDelegateTypeLocal = navArgsDelegateType
        return if (navArgsDelegateTypeLocal == null) {
            parameters.filter { it.isNavArg() }
        } else {
            if (navArgsDelegateTypeLocal.navArgs.any { !it.isNavArg() }) {
                throw IllegalDestinationsSetup("Composable '${composableName}': " +
                        "'$DESTINATION_ANNOTATION_NAV_ARGS_DELEGATE_ARGUMENT' cannot have arguments that are not navigation types.")
            }

            if (parameters.any { it.isNavArg() }) {
                throw IllegalDestinationsSetup("Composable '${composableName}': annotated " +
                        "function cannot define arguments of navigation type if using a '$DESTINATION_ANNOTATION_NAV_ARGS_DELEGATE_ARGUMENT' class.")
            }

            navArgsDelegateTypeLocal.navArgs
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
        if (isCustomTypeNavArg()) {
            return true
        }

        return isCoreType()
    }
}
