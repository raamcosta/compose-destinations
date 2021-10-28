package com.ramcosta.composedestinations.codegen.templates

import com.ramcosta.composedestinations.codegen.commons.*

//region anchors
const val DEFAULT_NAV_CONTROLLER_PLACEHOLDER = "[DEFAULT_NAV_CONTROLLER_PLACEHOLDER]"
const val EXPERIMENTAL_API_PLACEHOLDER = "[EXPERIMENTAL_API_PLACEHOLDER]"
const val ANIMATION_DEFAULT_PARAMS_PLACEHOLDER = "[ANIMATION_DEFAULT_PARAMS_PLACEHOLDER]"
const val ANIMATED_NAV_HOST_CALL_PARAMETERS_START = "[ANIMATED_NAV_HOST_CALL_PARAMETERS_START]"
const val ANIMATED_NAV_HOST_CALL_PARAMETERS_END = "[ANIMATED_NAV_HOST_CALL_PARAMETERS_END]"
const val INNER_NAV_HOST_CALL_ANIMATED_PARAMETERS_START = "[INNER_NAV_HOST_CALL_ANIMATED_PARAMETERS_START]"
const val INNER_NAV_HOST_CALL_ANIMATED_PARAMETERS_END = "[INNER_NAV_HOST_CALL_ANIMATED_PARAMETERS_END]"
const val NAV_HOST_METHOD_NAME = "[NAV_HOST_METHOD_NAME]"
const val ADD_ANIMATED_COMPOSABLE_START = "[ADD_ANIMATED_COMPOSABLE_START]"
const val ADD_ANIMATED_COMPOSABLE_END = "[ADD_ANIMATED_COMPOSABLE_END]"
const val ADD_BOTTOM_SHEET_COMPOSABLE_START = "[ADD_BOTTOM_SHEET_COMPOSABLE_START]"
const val ADD_BOTTOM_SHEET_COMPOSABLE_END = "[ADD_BOTTOM_SHEET_COMPOSABLE_END]"
const val ADD_COMPOSABLE_WHEN_ELSE_START = "[ADD_COMPOSABLES_WHEN_ELSE_START]"
const val ADD_COMPOSABLE_WHEN_ELSE_END = "[ADD_COMPOSABLES_WHEN_ELSE_END]"
const val ANIMATED_VISIBILITY_TO_CONTENT_START = "[ANIMATED_VISIBILITY_TO_CONTENT_START]"
const val ANIMATED_VISIBILITY_TO_CONTENT_END = "[ANIMATED_VISIBILITY_TO_CONTENT_END]"

const val START_ACCOMPANIST_NAVIGATION_IMPORTS = "//region accompanist navigation"
const val END_ACCOMPANIST_NAVIGATION_IMPORTS = "//endregion accompanist navigation"

const val START_ACCOMPANIST_MATERIAL_IMPORTS = "//region accompanist material"
const val END_ACCOMPANIST_MATERIAL_IMPORTS = "//endregion accompanist material"

//endregion

val destinationsNavHostTemplate = """
package $PACKAGE_NAME

import androidx.compose.animation.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import androidx.navigation.Navigator
import $PACKAGE_NAME.spec.DestinationSpec
import $PACKAGE_NAME.spec.DestinationStyle
import $PACKAGE_NAME.spec.NavGraphSpec
import $PACKAGE_NAME.navigation.DependenciesContainerBuilder
import $PACKAGE_NAME.navigation.dependency
$START_ACCOMPANIST_NAVIGATION_IMPORTS
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.navigation
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
$END_ACCOMPANIST_NAVIGATION_IMPORTS
$START_ACCOMPANIST_MATERIAL_IMPORTS
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
$END_ACCOMPANIST_MATERIAL_IMPORTS

//region NavHost
/**
 * Like [androidx.navigation.compose.NavHost] but uses composables annotated with
 * `@$DESTINATION_ANNOTATION` to pass in as the destinations available.
 *
 * @see [androidx.navigation.compose.NavHost]
 */
$EXPERIMENTAL_API_PLACEHOLDER@Composable
fun $DESTINATIONS_NAV_HOST(
    navGraph: $GENERATED_NAV_GRAPH = ${GENERATED_NAV_GRAPHS_OBJECT}.root,
    startDestination: $GENERATED_DESTINATION = navGraph.startDestination,$ANIMATION_DEFAULT_PARAMS_PLACEHOLDER
    navController: NavHostController = rememberDestinationsNavController(),
    modifier: Modifier = Modifier,
    dependenciesContainerBuilder: @Composable DependenciesContainerBuilder.($GENERATED_DESTINATION) -> Unit = {}
) {
    $NAV_HOST_METHOD_NAME(
        navController = navController,
        startDestination = startDestination.route,
        modifier = modifier,
        route = navGraph.route,$ANIMATED_NAV_HOST_CALL_PARAMETERS_START
        contentAlignment = defaultAnimationParams.contentAlignment,
        enterTransition = defaultAnimationParams.enterTransition?.run { { i, t -> enter(i.navDestination, t.navDestination) } },
        exitTransition = defaultAnimationParams.exitTransition?.run{ {i, t -> exit(i.navDestination, t.navDestination) } },
        popEnterTransition = defaultAnimationParams.popEnterTransition?.run{ {i, t -> enter(i.navDestination, t.navDestination) } },
        popExitTransition = defaultAnimationParams.popExitTransition?.run{ {i, t -> exit(i.navDestination, t.navDestination) } },$ANIMATED_NAV_HOST_CALL_PARAMETERS_END
    ) {
        addNavGraphDestinations(
            navGraphSpec = navGraph,
            addNavigation = addNavigation(),   
            addComposable = addComposable(navController, dependenciesContainerBuilder)
        )
    }
}
//endregion NavHost

//region NavController
/**
 * Wraps the correct `remember*NavController` method depending on
 * whether animations are available or not.
 */
$EXPERIMENTAL_API_PLACEHOLDER@Composable
fun rememberDestinationsNavController(
    vararg navigators: Navigator<out NavDestination>
) = $DEFAULT_NAV_CONTROLLER_PLACEHOLDER(*navigators)
//endregion

//region internals
${EXPERIMENTAL_API_PLACEHOLDER}private fun addComposable(
    navController: NavHostController,
    dependenciesContainerBuilder: @Composable DependenciesContainerBuilder.($GENERATED_DESTINATION) -> Unit
): NavGraphBuilder.($CORE_DESTINATION_SPEC) -> Unit {
    return { destination ->
        destination as $GENERATED_DESTINATION
        val destinationStyle = destination.style
        when (destinationStyle) {
            is DestinationStyle.Default -> {
                addComposable(
                    destination,
                    navController,
                    dependenciesContainerBuilder
                )
            }

            is DestinationStyle.Dialog -> {
                addDialogComposable(
                    destinationStyle,
                    destination,
                    navController,
                    dependenciesContainerBuilder
                )
            }
$ADD_ANIMATED_COMPOSABLE_START
            is DestinationStyle.Animated<*> -> {
                addAnimatedComposable(
                    destinationStyle as AnimatedDestinationStyle,
                    destination,
                    navController,
                    dependenciesContainerBuilder
                )
            }
$ADD_ANIMATED_COMPOSABLE_END$ADD_BOTTOM_SHEET_COMPOSABLE_START
            is DestinationStyle.BottomSheet -> {
                addBottomSheetComposable(
                    destination,
                    navController,
                    dependenciesContainerBuilder
                )
            }
$ADD_BOTTOM_SHEET_COMPOSABLE_END$ADD_COMPOSABLE_WHEN_ELSE_START
            else -> throw RuntimeException("Should be impossible! Code gen should have failed if using a style for which you don't have the dependency")
$ADD_COMPOSABLE_WHEN_ELSE_END        }
    }
}

${EXPERIMENTAL_API_PLACEHOLDER}private fun NavGraphBuilder.addComposable(
    destination: $GENERATED_DESTINATION,
    navController: NavHostController,
    dependenciesContainerBuilder: @Composable DependenciesContainerBuilder.($GENERATED_DESTINATION) -> Unit
) {
    composable(
        route = destination.route,
        arguments = destination.arguments,
        deepLinks = destination.deepLinks
    ) { navBackStackEntry ->
        destination.Content(
            navController,
            navBackStackEntry,
            { dependenciesContainerBuilder(destination)$ANIMATED_VISIBILITY_TO_CONTENT_START.apply {
                dependency<$ANIMATED_VISIBILITY_SCOPE_SIMPLE_NAME>(this@composable)
            }$ANIMATED_VISIBILITY_TO_CONTENT_END }
        )
    }
}

private fun NavGraphBuilder.addDialogComposable(
    dialogStyle: DestinationStyle.Dialog,
    destination: $GENERATED_DESTINATION,
    navController: NavHostController,
    dependenciesContainerBuilder: @Composable DependenciesContainerBuilder.($GENERATED_DESTINATION) -> Unit
) {
    dialog(
        destination.route,
        destination.arguments,
        destination.deepLinks,
        dialogStyle.properties
    ) {
        destination.Content(
            navController = navController,
            navBackStackEntry = it,
            dependenciesContainerBuilder = { dependenciesContainerBuilder(destination) }
        )
    }
}

${EXPERIMENTAL_API_PLACEHOLDER}private fun addNavigation(): NavGraphBuilder.($CORE_NAV_GRAPH_SPEC, NavGraphBuilder.() -> Unit) -> Unit {
    return { navGraph, builder ->
        navigation(
            navGraph.startDestination.route,
            navGraph.route
        ) {
            this.builder()
        }
    }
}
//endregion
""".trimIndent()