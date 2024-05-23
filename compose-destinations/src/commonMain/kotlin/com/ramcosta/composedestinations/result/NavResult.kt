package com.ramcosta.composedestinations.result

/**
 * Result sent from [ResultBackNavigator] when its destination is shown
 * and then navigates back to the [ResultRecipient] destination.
 *
 * If the destination related to the [ResultBackNavigator] doesn't put
 * any value using [ResultBackNavigator.setResult] or [ResultBackNavigator.navigateBack]
 * passing the result, then [Canceled] will be sent on [ResultRecipient.onNavResult],
 * otherwise the value set will be wrapped in [Value].
 */
sealed interface NavResult<out R> {
    data object Canceled: NavResult<Nothing>
    data class Value<R>(val value: R): NavResult<R>
}

/**
 * Returns [NavResult.Value.value] if there is any, or [canceledValue] result
 * if there isn't (i.e, it is [NavResult.Canceled])
 */
inline fun <R> NavResult<R>.getOr(canceledValue: () -> R): R = when (this) {
    is NavResult.Canceled -> canceledValue()
    is NavResult.Value -> value
}