package com.ramcosta.samples.playground

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.navigation
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.animations.rememberAnimatedNavHostEngine
import com.ramcosta.composedestinations.animations.utils.animatedComposable
import com.ramcosta.composedestinations.animations.utils.bottomSheetComposable
import com.ramcosta.composedestinations.manualcomposablecalls.ManualComposableCallsBuilder
import com.ramcosta.composedestinations.manualcomposablecalls.animatedComposable
import com.ramcosta.composedestinations.manualcomposablecalls.composable
import com.ramcosta.composedestinations.navigation.dependency
import com.ramcosta.composedestinations.scope.resultBackNavigator
import com.ramcosta.composedestinations.scope.resultRecipient
import com.ramcosta.composedestinations.utils.dialogComposable
import com.ramcosta.samples.playground.commons.DrawerController
import com.ramcosta.samples.playground.di.viewModel
import com.ramcosta.samples.playground.ui.screens.*
import com.ramcosta.samples.playground.ui.screens.destinations.*
import com.ramcosta.samples.playground.ui.screens.greeting.GreetingScreen
import com.ramcosta.samples.playground.ui.screens.greeting.GreetingUiEvents
import com.ramcosta.samples.playground.ui.screens.greeting.GreetingUiState
import com.ramcosta.samples.playground.ui.screens.greeting.GreetingViewModel
import com.ramcosta.samples.playground.ui.screens.profile.ProfileScreen
import com.ramcosta.samples.playground.ui.screens.profile.ProfileUiEvents
import com.ramcosta.samples.playground.ui.screens.profile.ProfileUiState
import com.ramcosta.samples.playground.ui.screens.profile.ProfileViewModel
import com.ramcosta.samples.playground.ui.screens.settings.SettingsScreen
import com.ramcosta.samples.playground.ui.screens.settings.SettingsViewModel
import com.ramcosta.samples.playground.ui.screens.settings.ThemeSettings

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialNavigationApi::class)
@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    drawerController: DrawerController,
    navController: NavHostController,
    testProfileDeepLink: () -> Unit,
) {
    // ------- Defining default animations for root and nested nav graphs example -------
//    val navHostEngine = rememberAnimatedNavHostEngine(
//        rootDefaultAnimations = RootNavGraphDefaultAnimations.ACCOMPANIST_FADING,
//        defaultAnimationsForNestedNavGraph = mapOf(
//            NavGraphs.settings to NestedNavGraphDefaultAnimations(
//                enterTransition = { fadeIn(animationSpec = tween(2000)) },
//                exitTransition = { fadeOut(animationSpec = tween(2000)) }
//            )
//        )
//    )

    val navHostEngine = rememberAnimatedNavHostEngine()

    DestinationsNavHost(
        navGraph = NavGraphs.root,
        startRoute = if (Math.random() > 0.5) FeedDestination else NavGraphs.root.startRoute,
        engine = navHostEngine,
        navController = navController,
        modifier = modifier,
        dependenciesContainerBuilder = {
            dependency(drawerController)

            dependency(NavGraphs.settings) {
                val parentEntry = remember(navBackStackEntry) {
                    navController.getBackStackEntry(NavGraphs.settings.route)
                }
                viewModel<SettingsViewModel>(parentEntry)
            }
        }
    ) {
        profileScreen()
        greetingScreen(testProfileDeepLink, drawerController)
    }
}

@ExperimentalAnimationApi
private fun ManualComposableCallsBuilder.profileScreen() {

    // animatedComposable is needed to get an AnimatedVisibilityScope to use as the receiver for our
    // ProfileScreen
    animatedComposable(ProfileScreenDestination) {
        val vm = viewModel<ProfileViewModel>()

        ProfileScreen(
            vm as ProfileUiState,
            vm as ProfileUiEvents,
            test = "testing param from NavHost"
        )
    }
}

private fun ManualComposableCallsBuilder.greetingScreen(
    testProfileDeepLink: () -> Unit,
    drawerController: DrawerController
) {
    composable(GreetingScreenDestination) {
        val vm = viewModel<GreetingViewModel>()

        GreetingScreen(
            navigator = destinationsNavigator,
            testProfileDeepLink = testProfileDeepLink,
            drawerController = drawerController,
            uiEvents = vm as GreetingUiEvents,
            uiState = vm as GreetingUiState,
            resultRecipient = resultRecipient(),
        )
    }
}

// region ------- Without using DestinationsNavHost example -------
@Suppress("UNUSED")
@ExperimentalMaterialNavigationApi
@ExperimentalAnimationApi
@Composable
fun SampleAppAnimatedNavHostExample(
    modifier: Modifier,
    navController: NavHostController,
    drawerController: DrawerController,
    testProfileDeepLink: () -> Unit
) {
    AnimatedNavHost(
        modifier = modifier,
        navController = navController,
        startDestination = GreetingScreenDestination.route,
        route = "root"
    ) {

        animatedComposable(GreetingScreenDestination) {
            val vm = viewModel<GreetingViewModel>()

            GreetingScreen(
                navigator = destinationsNavigator(navController),
                drawerController = drawerController,
                uiEvents = vm as GreetingUiEvents,
                uiState = vm as GreetingUiState,
                resultRecipient = resultRecipient(),
                testProfileDeepLink = testProfileDeepLink
            )
        }

        animatedComposable(FeedDestination) {
            Feed(destinationsNavigator(navController))
        }

        dialogComposable(GoToProfileConfirmationDestination) {
            GoToProfileConfirmation(
                resultNavigator = resultBackNavigator(navController)
            )
        }

        animatedComposable(TestScreenDestination) {
            TestScreen(
                id = navArgs.id,
                asd = navArgs.asd,
                stuff1 = navArgs.stuff1,
                stuff2 = navArgs.stuff2,
                stuff3 = navArgs.stuff3,
                stuff5 = navArgs.stuff5,
                stuff6 = navArgs.stuff6,
            )
        }

        animatedComposable(ProfileScreenDestination) {
            val vm = viewModel<ProfileViewModel>()

            ProfileScreen(
                vm as ProfileUiState,
                vm as ProfileUiEvents,
                test = "testing param from NavHost"
            )
        }

        navigation(
            startDestination = SettingsScreenDestination.route,
            route = "settings"
        ) {
            animatedComposable(SettingsScreenDestination) {
                SettingsScreen(
                    viewModel = viewModel(),
                    navigator = destinationsNavigator(navController),
                    themeSettingsResultRecipient = resultRecipient()
                )
            }

            bottomSheetComposable(ThemeSettingsDestination) {
                ThemeSettings(
                    viewModel = viewModel(),
                    resultNavigator = resultBackNavigator(navController)
                )
            }
        }
    }
}
// endregion
