package com.ramcosta.composedestinations.spec

import android.os.Bundle
import androidx.annotation.RestrictTo
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDeepLink

typealias Route = TypedRoute<*>

/**
 * Interface for all classes which instances
 * are identified by a route.
 *
 * All [TypedDestinationSpec] and [TypedNavGraphSpec] are
 * [TypedRoute].
 *
 * [TypedRoute] instances are not suited to navigate
 * to unless they're also [Direction].
 */
sealed interface TypedRoute<T> {

    /**
     * Full route that will be added to the navigation graph
     */
    val route: String

    /**
     * Prefix of the route - basically [route] without argument info.
     * Meant for internal usage only.
     */
    @get:RestrictTo(RestrictTo.Scope.SUBCLASSES)
    val baseRoute: String

    /**
     * All [NamedNavArgument]s that will be added to the navigation
     * graph for this destination
     */
    val arguments: List<NamedNavArgument> get() = emptyList()

    /**
     * All [NavDeepLink]s that will be added to the navigation
     * graph for this destination
     */
    val deepLinks: List<NavDeepLink> get() = emptyList()

    /**
     * Function to get a [Direction] you can then pass to [com.ramcosta.composedestinations.navigation.DestinationsNavigator]
     * or to [NavController].navigateTo() to safely navigate to this Destination.
     */
    operator fun invoke(navArgs: T): Direction

    /**
     * Method that returns the navigation arguments class of this Composable
     * for the [navBackStackEntry] when the destination gets navigated to.
     */
    fun argsFrom(navBackStackEntry: NavBackStackEntry) : T = argsFrom(navBackStackEntry.arguments)

    /**
     * Method that returns the navigation arguments class of this Composable
     * for the [bundle] when the destination gets navigated to.
     */
    fun argsFrom(bundle: Bundle?) : T

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
}
