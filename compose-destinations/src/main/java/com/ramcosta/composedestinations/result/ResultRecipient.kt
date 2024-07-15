@file:SuppressLint("ComposableNaming")
@file:Suppress("UNCHECKED_CAST")

package com.ramcosta.composedestinations.result

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.Stable
import com.ramcosta.composedestinations.spec.DestinationSpec

/**
 * Recipient where you can install a listener to be notified of results (of type [R])
 * from a specific [DestinationSpec] (of type [D]).
 *
 * If declared as a parameter of a `@Destination` annotated Composable,
 * Compose Destinations will provide a correct implementation.
 *
 * If you're manually calling that Composable, then you can use
 * [com.ramcosta.composedestinations.scope.resultRecipient]
 * extension function to get a correctly typed implementation.
 * To get a correctly typed [com.ramcosta.composedestinations.navargs.DestinationsNavType]
 * in this case, check what the generated Destination object uses.
 *
 * Type safety related limitations (compile time enforced):
 * - [R] must be a valid navigation argument type.
 * - Each annotated Composable can have at most one parameter of type [ResultRecipient] for a given [DestinationSpec] ([D])
 * - [D] destination Composable must have a corresponding [ResultBackNavigator] of the same type [R]
 *
 * @see [com.ramcosta.composedestinations.result.ResultBackNavigator]
 */
@Stable
interface ResultRecipient<D : DestinationSpec, R> : OpenResultRecipient<R> {

    /**
     * Install a [listener] that will be called when the [D] destination
     * finishes. [NavResult] will either contain a specific result [R] for [NavResult.Value]
     * or it will be [NavResult.Canceled] if [D] finishes without setting any result.
     *
     * Implementation makes sure to only do something the first time it is called,
     * so, no need to worry about recomposition.
     *
     * [listener] will not be called from a Compose scope, it should be treated
     * as a normal button click listener, you can navigate or call a method on a view model,
     * for example.
     */
    @Composable
    override fun onNavResult(listener: @DisallowComposableCalls (NavResult<R>) -> Unit)
}
