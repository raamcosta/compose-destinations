package com.ramcosta.composedestinations.codegen.templates

import com.ramcosta.composedestinations.codegen.commons.*

val sealedDestinationTemplate = """
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
 * When using the code gen module, all APIs will expose
 * $GENERATED_DESTINATION which is a sealed version of [$CORE_DESTINATION_SPEC]
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
 */
fun NavBackStackEntry.toDest() = destination.route?.let { $DESTINATIONS_AGGREGATE_CLASS_NAME.${GENERATED_NAV_GRAPH}s.root.findDestination(it) as $GENERATED_DESTINATION }

""".trimIndent()