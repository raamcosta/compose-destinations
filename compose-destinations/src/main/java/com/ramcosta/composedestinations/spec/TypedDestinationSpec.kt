package com.ramcosta.composedestinations.spec

import androidx.annotation.RestrictTo
import androidx.compose.runtime.Composable
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.*
import com.ramcosta.composedestinations.scope.DestinationScope
import com.ramcosta.composedestinations.navigation.DependenciesContainerBuilder


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
     * Method that returns the navigation arguments class of this Composable
     * for the [navBackStackEntry] when the destination gets navigated to.
     */
    fun argsFrom(navBackStackEntry: NavBackStackEntry) : T

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
    fun argsFrom(savedStateHandle: SavedStateHandle) : T

    /**
     * Full route that will be added to the navigation graph
     */
    override val route: String

    /**
     * Prefix of the route - basically [route] without argument info.
     * Meant for internal usage only.
     */
    @get:RestrictTo(RestrictTo.Scope.SUBCLASSES)
    val baseRoute: String

    /**
     * Style of this destination. It can be one of:
     * - [DestinationStyle.Default]
     * - [DestinationStyle.Animated]
     * - [DestinationStyle.BottomSheet]
     * - [DestinationStyle.Dialog]
     * - [DestinationStyle.Runtime]
     */
    val style: DestinationStyle get() = DestinationStyle.Default

    /**
     * [Composable] function that will be called to compose
     * the destination content in the screen, when the user
     * navigates to it.
     */
    @Composable
    fun DestinationScope<T>.Content(
        dependenciesContainerBuilder: @Composable DependenciesContainerBuilder<T>.() -> Unit
    )
}
