package com.ramcosta.composedestinations.codegen.templates

import com.ramcosta.composedestinations.codegen.commons.*

const val TRANSITION_TYPE_START_PLACEHOLDER = "[TRANSITION_TYPE_START_PLACEHOLDER]"
const val TRANSITION_TYPE_END_PLACEHOLDER = "[TRANSITION_TYPE_END_PLACEHOLDER]"

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
sealed interface $GENERATED_DESTINATION : $CORE_DESTINATION_SPEC $TRANSITION_TYPE_START_PLACEHOLDER{

    @ExperimentalAnimationApi
    val transitionType: TransitionType get() = TransitionType.None
}

sealed class TransitionType {
    @ExperimentalAnimationApi
    class Animation(val destinationTransitions: $GENERATED_DESTINATION_TRANSITIONS) : TransitionType()
    object None : TransitionType()
}$TRANSITION_TYPE_END_PLACEHOLDER

@ExperimentalAnimationApi
interface $GENERATED_DESTINATION_TRANSITIONS {

    val enterTransition: (AnimatedContentScope<String>.(initial: $GENERATED_DESTINATION?, target: $GENERATED_DESTINATION?) -> EnterTransition?)? get() = null

    val exitTransition: (AnimatedContentScope<String>.(initial: $GENERATED_DESTINATION?, target: $GENERATED_DESTINATION?) -> ExitTransition?)? get() = null

    val popEnterTransition: (AnimatedContentScope<String>.(initial: $GENERATED_DESTINATION?, target: $GENERATED_DESTINATION?) -> EnterTransition?)? get() = enterTransition

    val popExitTransition: (AnimatedContentScope<String>.(initial: $GENERATED_DESTINATION?, target: $GENERATED_DESTINATION?) -> ExitTransition?)? get() = exitTransition

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