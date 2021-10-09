package com.ramcosta.composedestinations.codegen.templates

import com.ramcosta.composedestinations.codegen.commons.*

val sealedDestinationTemplate = """
package $PACKAGE_NAME

import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import $PACKAGE_NAME.navigation.Routed
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi

/**
 * When using the code gen module, all APIs will expose
 * $GENERATED_DESTINATION which is a sealed version of [$CORE_DESTINATION_SPEC]
 */
sealed interface $GENERATED_DESTINATION : $CORE_DESTINATION_SPEC {

    val style: DestinationStyle get() = DestinationStyle.Default
}

@ExperimentalAnimationApi
interface $GENERATED_ANIMATED_DESTINATION_STYLE : $CORE_DESTINATION_TRANSITIONS<$GENERATED_DESTINATION> {

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
 * Navigates to the [$GENERATED_NAV_GRAPH] or [$GENERATED_DESTINATION].
 */
fun NavController.navigateTo(
    routed: Routed,
    navOptionsBuilder: NavOptionsBuilder.() -> Unit = {}
) {
    navigate(routed.route, navOptionsBuilder)
}

""".trimIndent()