package com.ramcosta.composedestinations.spec

import androidx.compose.runtime.Composable
import androidx.core.bundle.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavBackStackEntry
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

    /**
     * Method that returns the navigation arguments class of this Composable
     * for the [bundle] when the destination gets navigated to.
     */
    override fun argsFrom(bundle: Bundle?) : T

    /**
     * Method that returns the navigation arguments class of this Composable
     * for the [savedStateHandle]. This is useful when the [SavedStateHandle]
     * is created with the navigation arguments, for example, inside the
     * ViewModel.
     *
     * If you're manually creating the ViewModel, use the `AbstractSavedStateViewModelFactory`
     * and pass the [NavBackStackEntry.arguments] as the second constructor parameter.
     * If you're using something like Hilt, then that is done for you out of the box.
     */
    override fun argsFrom(savedStateHandle: SavedStateHandle) : T

    /**
     * Method that returns the navigation arguments class of this Composable
     * for the [navBackStackEntry] when the destination gets navigated to.
     */
    override fun argsFrom(navBackStackEntry: NavBackStackEntry) : T = argsFrom(navBackStackEntry.arguments)
}
