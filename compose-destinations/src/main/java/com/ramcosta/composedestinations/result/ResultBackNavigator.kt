package com.ramcosta.composedestinations.result

import androidx.navigation.NavController

/**
 * Navigator that allows navigating back while passing
 * a result of type [R].
 *
 * If declared as a parameter of a `@Destination` annotated Composable,
 * Compose Destinations will provide a correct implementation.
 *
 * If you're manually calling that Composable, then you can use
 * [com.ramcosta.composedestinations.scope.resultBackNavigator]
 * extension function to get a correctly typed implementation.
 * To get a correctly typed [com.ramcosta.composedestinations.navargs.DestinationsNavType]
 * in this case, check what the generated Destination object uses.
 *
 * Type safety related limitations (compile time enforced):
 * - [R] must be a valid navigation argument type.
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
     */
    fun navigateBack(result: R)

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
     * or just navigating if no result was set.
     *
     * It uses [NavController.navigateUp] internally to go back.
     */
    fun navigateBack()
}
