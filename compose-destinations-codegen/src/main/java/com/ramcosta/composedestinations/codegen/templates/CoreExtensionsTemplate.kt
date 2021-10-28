package com.ramcosta.composedestinations.codegen.templates

import com.ramcosta.composedestinations.codegen.commons.*

val coreExtensionsTemplate = """
package $PACKAGE_NAME

import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import $PACKAGE_NAME.spec.DestinationSpec
import $PACKAGE_NAME.spec.DestinationStyle
import $PACKAGE_NAME.spec.NavGraphSpec
import androidx.navigation.NavBackStackEntry
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi

/**
 * $GENERATED_DESTINATION is a sealed version of [$CORE_DESTINATION_SPEC]
 */
sealed interface $GENERATED_DESTINATION : $CORE_DESTINATION_SPEC

/**
 * Version of [$CORE_DESTINATION_ANIMATION_STYLE] used to expose methods with 
 * [$GENERATED_DESTINATION]. 
 * 
 * Create subclasses to control animation behaviour when transitioning
 * between destinations.
 */
@ExperimentalAnimationApi
interface $GENERATED_ANIMATED_DESTINATION_STYLE : $CORE_DESTINATION_ANIMATION_STYLE<$GENERATED_DESTINATION> {

    override fun AnimatedContentScope<String>.enterTransition(
        initial: $GENERATED_DESTINATION?,
        target: $GENERATED_DESTINATION?
    ): EnterTransition? {
        return null
    }

    override fun AnimatedContentScope<String>.exitTransition(
        initial: $GENERATED_DESTINATION?,
        target: $GENERATED_DESTINATION?
    ): ExitTransition? {
        return null
    }

    override fun AnimatedContentScope<String>.popEnterTransition(
        initial: $GENERATED_DESTINATION?,
        target: $GENERATED_DESTINATION?
    ): EnterTransition? {
        return enterTransition(initial, target)
    }

    override fun AnimatedContentScope<String>.popExitTransition(
        initial: $GENERATED_DESTINATION?,
        target: $GENERATED_DESTINATION?
    ): ExitTransition? {
        return exitTransition(initial, target)
    }
}

/**
 * Realization of [$CORE_NAV_GRAPH_SPEC] for the app.
 * It uses [$GENERATED_DESTINATION] instead of [$CORE_DESTINATION_SPEC].
 * 
 * @see [$CORE_NAV_GRAPH_SPEC]
 */
data class $GENERATED_NAV_GRAPH(
    override val route: String,
    override val startDestination: $GENERATED_DESTINATION,
    override val destinations: Map<String, $GENERATED_DESTINATION>,
    override val nestedNavGraphs: List<$GENERATED_NAV_GRAPH> = emptyList()
): $CORE_NAV_GRAPH_SPEC

/**
 * Finds the destination correspondent to this [NavBackStackEntry], null if none is found
 * or if no route is set in this back stack entry's destination.
 */
val NavBackStackEntry.navDestination: $GENERATED_DESTINATION?
    get() {
        return destination.route?.let { $GENERATED_NAV_GRAPHS_OBJECT.root.findDestination(it) as $GENERATED_DESTINATION }
    }
""".trimIndent()