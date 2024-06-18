package com.ramcosta.samples.playground

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.manualcomposablecalls.ManualComposableCallsBuilder
import com.ramcosta.composedestinations.manualcomposablecalls.addDeepLink
import com.ramcosta.composedestinations.manualcomposablecalls.composable
import com.ramcosta.composedestinations.navigation.dependency
import com.ramcosta.composedestinations.navigation.destination
import com.ramcosta.composedestinations.navigation.navGraph
import com.ramcosta.composedestinations.scope.resultRecipient
import com.ramcosta.composedestinations.spec.DestinationStyle
import com.ramcosta.composedestinations.utils.rememberDestinationsNavigator
import com.ramcosta.samples.playground.commons.DrawerController
import com.ramcosta.samples.playground.di.viewModel
import com.ramcosta.samples.playground.ui.screens.NavGraphs
import com.ramcosta.samples.playground.ui.screens.destinations.FeedDestination
import com.ramcosta.samples.playground.ui.screens.destinations.GreetingScreenDestination
import com.ramcosta.samples.playground.ui.screens.destinations.ProfileScreenDestination
import com.ramcosta.samples.playground.ui.screens.destinations.TestScreenDestination
import com.ramcosta.samples.playground.ui.screens.greeting.GreetingScreen
import com.ramcosta.samples.playground.ui.screens.greeting.GreetingUiEvents
import com.ramcosta.samples.playground.ui.screens.greeting.GreetingUiState
import com.ramcosta.samples.playground.ui.screens.greeting.GreetingViewModel
import com.ramcosta.samples.playground.ui.screens.profile.ProfileViewModel
import com.ramcosta.samples.playground.ui.screens.settings.SettingsViewModel

//@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    drawerController: DrawerController,
    navController: NavHostController,
    testProfileDeepLink: () -> Unit,
) {
    val destinationsNavigator = navController.rememberDestinationsNavigator()
//    SharedTransitionLayout {
        DestinationsNavHost(
            navGraph = NavGraphs.root,
            startRoute = if (Math.random() > 0.5) FeedDestination else NavGraphs.root.startRoute,
            navController = navController,
            modifier = modifier,
            dependenciesContainerBuilder = {
//                dependency(this@SharedTransitionLayout)

                dependency(drawerController)

                destination(ProfileScreenDestination) {
                    dependency(viewModel<ProfileViewModel>())
                }

                navGraph(NavGraphs.settings) {
                    val parentEntry = remember(navBackStackEntry) {
                        destinationsNavigator.getBackStackEntry(NavGraphs.settings)!!
                    }

                    dependency(viewModel<SettingsViewModel>(parentEntry))
                }
            }
        ) {
            addDeepLink(TestScreenDestination) { uriPattern = "runtimeschema://${TestScreenDestination.route}" }

            TestScreenDestination animateWith object: DestinationStyle.Animated() {
                override val enterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition =
                    { fadeIn(tween(5000)) }
                override val exitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition =
                    { fadeOut(tween(2000)) }
            }

            greetingScreen(testProfileDeepLink, drawerController)
        }
//    }
}

//@OptIn(ExperimentalSharedTransitionApi::class)
private fun ManualComposableCallsBuilder.greetingScreen(
//    sharedTransitionScope: SharedTransitionScope,
    testProfileDeepLink: () -> Unit,
    drawerController: DrawerController
) {
    composable(GreetingScreenDestination) {
        val vm = viewModel<GreetingViewModel>()

//        sharedTransitionScope.
        GreetingScreen(
            animatedVisibilityScope = this,
            navigator = destinationsNavigator,
            testProfileDeepLink = testProfileDeepLink,
            drawerController = drawerController,
            uiEvents = vm as GreetingUiEvents,
            uiState = vm as GreetingUiState,
            test = "testing param from NavHost",
            resultRecipient = resultRecipient(),
            featYResult = resultRecipient(),
        )
    }
}
