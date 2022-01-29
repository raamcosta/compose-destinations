@file:SuppressLint("ComposableNaming")
@file:Suppress("UNCHECKED_CAST")

package com.ramcosta.composedestinations.result

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.remember
import androidx.navigation.NavBackStackEntry
import com.ramcosta.composedestinations.spec.DestinationSpec

/**
 * Recipient where you can install a listener to be notified of results (of type [R])
 * from a specific [DestinationSpec] (of type [D]).
 *
 * If declared as a parameter of a `@Destination` annotated Composable,
 * Compose Destinations will provide a correct implementation. If you're
 * manually calling that Composable, then you can use
 * [com.ramcosta.composedestinations.manualcomposablecalls.resultRecipient]
 * extension function to get a correctly typed implementation.
 *
 * Type safety related limitations (compile time enforced):
 * - [R] must be one of String, Boolean, Float, Int, Long, Serializable, or Parcelable.
 * They can be nullable.
 * - [R] type cannot have type arguments itself (f.e you can't use Array<String> even though it is Serializable)
 * - Each annotated Composable can have at most one parameter of type [ResultRecipient] for a given [DestinationSpec] ([D])
 * - [D] destination Composable must have a corresponding [ResultBackNavigator] of the same type [R]
 *
 * @see [com.ramcosta.composedestinations.result.ResultBackNavigator]
 */
interface ResultRecipient<D : DestinationSpec<*>, R> {

    /**
     * Install a [listener] that will be called when the [D] destination
     * finishes with a specific result [R].
     *
     * Implementation makes sure to only do something the first time it is called,
     * so, no need to worry about recomposition.
     *
     * [listener] will not be called from a Compose scope, it should be treated
     * as a normal button click listener, you can navigate or call a method on a view model.
     */
    @Composable
    fun onResult(listener: @DisallowComposableCalls (R) -> Unit)
}

/**
 * Returns a well typed [ResultRecipient] for [navBackStackEntry].
 * You shouldn't have to call this directly, it is public to be used in the generated code.
 *
 * If you're manually calling your Composable, then use
 * [com.ramcosta.composedestinations.manualcomposablecalls.resultRecipient] instead.
 */
@Composable
inline fun <reified D : DestinationSpec<*>, reified R> resultRecipient(
    navBackStackEntry: NavBackStackEntry
): ResultRecipient<D, R> = remember {
    ResultRecipientImpl(
        navBackStackEntry = navBackStackEntry,
        resultOriginType = D::class.java,
        resultType = R::class.java,
    )
}
