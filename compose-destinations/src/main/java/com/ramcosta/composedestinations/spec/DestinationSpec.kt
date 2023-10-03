package com.ramcosta.composedestinations.spec

import androidx.compose.runtime.Composable
import com.ramcosta.composedestinations.scope.DestinationScope


typealias DestinationSpec = TypedDestinationSpec<*>

/**
 * Defines what a Destination needs to have to be able to be
 * added to a navigation graph and composed on the screen
 * when the user navigates to it.
 *
 * [T] is the type of the class that holds all navigation arguments
 * for of this Destination.
 */
interface TypedDestinationSpec<T> : TypedRoute<T> {

    /**
     * Style of this destination. It can be one of:
     * - [DestinationStyle.Default]
     * - [DestinationStyle.Dialog]
     * - [DestinationStyle.Animated]
     * And when using "bottom-sheet" dependency, it can also be:
     * - `DestinationStyleBottomSheet`
     */
    val style: DestinationStyle get() = DestinationStyle.Default

    /**
     * [Composable] function that will be called to compose
     * the destination content in the screen, when the user
     * navigates to it.
     */
    @Composable
    fun DestinationScope<T>.Content()
}
