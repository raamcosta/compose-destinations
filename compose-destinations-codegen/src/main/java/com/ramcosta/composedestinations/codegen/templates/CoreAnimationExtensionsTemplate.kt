package com.ramcosta.composedestinations.codegen.templates

import com.ramcosta.composedestinations.codegen.commons.DESTINATIONS_AGGREGATE_CLASS_NAME
import com.ramcosta.composedestinations.codegen.commons.GENERATED_DESTINATION
import com.ramcosta.composedestinations.codegen.commons.GENERATED_NAV_GRAPH
import com.ramcosta.composedestinations.codegen.commons.PACKAGE_NAME

val coreAnimationsExtensionsTemplate = """
package $PACKAGE_NAME

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.ui.Alignment
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.composable

@ExperimentalAnimationApi
fun NavGraphBuilder.addAnimatedComposable(
    animatedStyle: AnimatedDestinationStyle,
    destination: Destination,
    navController: NavHostController,
    situationalParametersProvider: ($GENERATED_DESTINATION) -> MutableMap<Class<*>, Any>
) = with(animatedStyle) {
    composable(
        route = destination.route,
        arguments = destination.arguments,
        deepLinks = destination.deepLinks,
        enterTransition = { i, t -> enterTransition(i.toDest(), t.toDest()) },
        exitTransition = { i, t -> exitTransition(i.toDest(), t.toDest()) },
        popEnterTransition = { i, t -> popEnterTransition(i.toDest(), t.toDest()) },
        popExitTransition = { i, t -> popExitTransition(i.toDest(), t.toDest()) }
    ) { navBackStackEntry ->
        destination.Content(
            navController,
            navBackStackEntry,
            situationalParametersProvider(destination).apply {
                this[AnimatedVisibilityScope::class.java] = this@composable
            }
        )
    }
}

@ExperimentalAnimationApi
class AnimationDefaultParams(
    val contentAlignment: Alignment = Alignment.Center,
    val enterTransition: (AnimatedContentScope<String>.(initial: Destination?, target: Destination?) -> EnterTransition)? =
        { _, _ -> fadeIn(animationSpec = tween(700)) },
    val exitTransition: (AnimatedContentScope<String>.(initial: Destination?, target: Destination?) -> ExitTransition)? =
        { _, _ -> fadeOut(animationSpec = tween(700)) },
    val popEnterTransition: (AnimatedContentScope<String>.(initial: Destination?, target: Destination?) -> EnterTransition)? = enterTransition,
    val popExitTransition: (AnimatedContentScope<String>.(initial: Destination?, target: Destination?) -> ExitTransition)? = exitTransition,
)

@ExperimentalAnimationApi
fun NavBackStackEntry.toDest() = destination.route?.let { $DESTINATIONS_AGGREGATE_CLASS_NAME.${GENERATED_NAV_GRAPH}s.root.findDestination(it) as $GENERATED_DESTINATION }
""".trimIndent()