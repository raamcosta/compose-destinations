package com.ramcosta.composedestinations.result

import androidx.compose.runtime.*
import androidx.navigation.NavController

/**
 * Navigator that allows navigating back while passing
 * a result of type [R].
 *
 * If declared as a parameter of a `@Destination` annotated Composable,
 * Compose Destinations will provide a correct implementation. If you're
 * manually calling that Composable, then you can use
 * [com.ramcosta.composedestinations.manualcomposablecalls.resultBackNavigator]
 * extension function to get a correctly typed implementation.
 *
 * Type safety related limitations (compile time enforced):
 * - [R] must be one of String, Boolean, Float, Int, Long, Serializable, or Parcelable.
 * They can be nullable.
 * - [R] type cannot have type arguments itself (f.e you can't use Array<String> even though it is Serializable)
 * - Each annotated Composable can have at most one parameter of type [ResultBackNavigator]
 *
 * @see [com.ramcosta.composedestinations.result.ResultRecipient]
 */
interface ResultBackNavigator<R> {

    /**
     * Goes back to previous destination sending [result].
     *
     * It uses [NavController.navigateUp] internally to go back.
     *
     * Check [com.ramcosta.composedestinations.result.ResultRecipient] to see
     * how to get the result.
     *
     * @param onlyIfResumed if true, will ignore the navigation action if the current `NavBackStackEntry`
     * is not in the RESUMED state. This avoids duplicate navigation actions.
     * By default is false to have the same behaviour as [NavController].
     */
    fun navigateBack(
        result: R,
        onlyIfResumed: Boolean = false
    )

    /**
     * Sets a [result] to be sent on the next [navigateBack] call.
     *
     * Check [com.ramcosta.composedestinations.result.ResultRecipient] to see
     * how to get the result.
     *
     * If multiple calls are done, the last one will be the result sent back.
     * This also applies if you call [navigateBack] (with result) after calling this.
     */
    fun setResult(result: R)

    /**
     * Goes back to previous destination sending the last result set with [setResult]
     * or just navigating if no result was set..
     *
     * It uses [NavController.navigateUp] internally to go back.
     *
     * @param onlyIfResumed if true, will ignore the navigation action if the current `NavBackStackEntry`
     * is not in the RESUMED state. This avoids duplicate navigation actions.
     * By default is false to have the same behaviour as [NavController].
     */
    fun navigateBack(onlyIfResumed: Boolean = false)
}
