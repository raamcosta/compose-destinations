package com.ramcosta.composedestinations.result

import androidx.navigation.NavController
import com.ramcosta.composedestinations.spec.DestinationSpec

/**
 * Internal details, public only for inline functions.
 *
 *  @see [ResultBackNavigator].
 */
class ResultBackNavigatorImpl<R>(
    private val navController: NavController,
    resultOriginType: Class<out DestinationSpec<*>>,
    resultType: Class<R>
) : ResultBackNavigator<R> {

    private val resultKey = resultKey(resultOriginType, resultType)

    override fun navigateBack(result: R) {
        setResult(result)
        navigateBack()
    }

    override fun setResult(result: R) {
        navController.previousBackStackEntry
            ?.savedStateHandle
            ?.set(resultKey, result)
    }

    override fun navigateBack() {
        navController.navigateUp()
    }
}