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
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.generated.featurey.navtype.internalBackResultNavType
import com.ramcosta.composedestinations.manualcomposablecalls.ManualComposableCallsBuilder
import com.ramcosta.composedestinations.manualcomposablecalls.composable
import com.ramcosta.composedestinations.navargs.primitives.DestinationsBooleanNavType
import com.ramcosta.composedestinations.navigation.dependency
import com.ramcosta.composedestinations.navigation.destination
import com.ramcosta.composedestinations.navigation.navGraph
import com.ramcosta.composedestinations.scope.resultRecipient
import com.ramcosta.composedestinations.spec.DestinationStyle
import com.ramcosta.composedestinations.utils.rememberDestinationsNavigator
import com.ramcosta.samples.playground.commons.DrawerController
import com.ramcosta.samples.playground.ui.screens.NavGraphs
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
    val toaster = LocalToaster.current
    val destinationsNavigator = navController.rememberDestinationsNavigator()
//    SharedTransitionLayout {
    val diContainer = LocalDIContainer.current
    DestinationsNavHost(
            navGraph = NavGraphs.root,
            navController = navController,
            modifier = modifier,
            dependenciesContainerBuilder = {
//                dependency(this@SharedTransitionLayout)

                dependency(toaster)
                dependency(drawerController)

                destination(ProfileScreenDestination) {
                    dependency(
                        viewModel {
                            ProfileViewModel(diContainer.getProfileLikeCount, createSavedStateHandle())
                        }
                    )
                }

                navGraph(NavGraphs.settings) {
                    val parentEntry = remember(navBackStackEntry) {
                        destinationsNavigator.getBackStackEntry(NavGraphs.settings)!!
                    }

                    dependency(viewModel(parentEntry) { SettingsViewModel() })
                }
            }
        ) {
            addPlatformDependentDeepLinks()

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

expect fun ManualComposableCallsBuilder.addPlatformDependentDeepLinks()

//@OptIn(ExperimentalSharedTransitionApi::class)
private fun ManualComposableCallsBuilder.greetingScreen(
//    sharedTransitionScope: SharedTransitionScope,
    testProfileDeepLink: () -> Unit,
    drawerController: DrawerController
) {
    composable(GreetingScreenDestination) {
        val vm = viewModel { GreetingViewModel() }

//        sharedTransitionScope.
        GreetingScreen(
            animatedVisibilityScope = this,
            navigator = destinationsNavigator,
            testProfileDeepLink = testProfileDeepLink,
            drawerController = drawerController,
            uiEvents = vm as GreetingUiEvents,
            uiState = vm as GreetingUiState,
            test = "testing param from NavHost",
            resultRecipient = resultRecipient(DestinationsBooleanNavType),
            featYResult = resultRecipient(internalBackResultNavType),
        )
    }
}
