package com.ramcosta.samples.destinationstodosample

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.navigation
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.animations.rememberAnimatedNavHostEngine
import com.ramcosta.composedestinations.animations.utils.animatedComposable
import com.ramcosta.composedestinations.animations.utils.bottomSheetComposable
import com.ramcosta.composedestinations.manualcomposablecalls.*
import com.ramcosta.composedestinations.navigation.DestinationsNavController
import com.ramcosta.composedestinations.navigation.dependency
import com.ramcosta.composedestinations.result.resultBackNavigator
import com.ramcosta.composedestinations.result.resultRecipient
import com.ramcosta.composedestinations.utils.dialogComposable
import com.ramcosta.samples.destinationstodosample.commons.DrawerController
import com.ramcosta.samples.destinationstodosample.ui.screens.*
import com.ramcosta.samples.destinationstodosample.ui.screens.destinations.*
import com.ramcosta.samples.destinationstodosample.ui.screens.greeting.GreetingScreen
import com.ramcosta.samples.destinationstodosample.ui.screens.greeting.GreetingUiEvents
import com.ramcosta.samples.destinationstodosample.ui.screens.greeting.GreetingUiState
import com.ramcosta.samples.destinationstodosample.ui.screens.greeting.GreetingViewModel
import com.ramcosta.samples.destinationstodosample.ui.screens.profile.ProfileScreen
import com.ramcosta.samples.destinationstodosample.ui.screens.profile.ProfileUiEvents
import com.ramcosta.samples.destinationstodosample.ui.screens.profile.ProfileUiState
import com.ramcosta.samples.destinationstodosample.ui.screens.profile.ProfileViewModel

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialNavigationApi::class)
@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    drawerController: DrawerController,
    navController: NavHostController,
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
        }
    ) {
        // region This is NOT needed: this is exactly what the lib would do for us too
        // if we didn't explicitly call this Composable
        // It is here only as an example of getting nav args in a type safe way at this point
        // and also so we can see the boilerplate we save for each destination
        composable(TestScreenDestination) {
            TestScreen(
                id = navArgs.id,
                stuff1 = navArgs.stuff1,
                stuff2 = navArgs.stuff2,
                stuff3 = navArgs.stuff3
            )
        }
        // endregion

        // region Composables we need to call ourselves since the lib doesn't know how to get
        // DrawerController or the *UiState and *UiEvents interfaces

        // animatedComposable is needed to get an AnimatedVisibilityScope to use as the receiver for our
        // ProfileScreen
        animatedComposable(ProfileScreenDestination) {
            val vm = viewModel<ProfileViewModel>(
                factory = ProfileViewModel.Factory(navBackStackEntry)
            )

            ProfileScreen(
                vm as ProfileUiState,
                vm as ProfileUiEvents
            )
        }

        composable(GreetingScreenDestination) {
            val vm = viewModel<GreetingViewModel>()

            GreetingScreen(
                navigator = destinationsNavigator,
                drawerController = drawerController,
                uiEvents = vm as GreetingUiEvents,
                uiState = vm as GreetingUiState,
                resultRecipient = resultRecipient()
            )
        }
        // endregion
    }
}

// ------- Without using DestinationsNavHost example -------
@Suppress("UNUSED")
@ExperimentalMaterialNavigationApi
@ExperimentalAnimationApi
@Composable
fun SampleAppAnimatedNavHostExample(
    modifier: Modifier,
    navController: NavHostController,
    drawerController: DrawerController
) {
    AnimatedNavHost(
        modifier = modifier,
        navController = navController,
        startDestination = GreetingScreenDestination.route,
        route = "root"
    ) {

        animatedComposable(GreetingScreenDestination) { _, entry ->
            val vm = viewModel<GreetingViewModel>()

            GreetingScreen(
                navigator = DestinationsNavController(navController, entry),
                drawerController = drawerController,
                uiEvents = vm as GreetingUiEvents,
                uiState = vm as GreetingUiState,
                resultRecipient = resultRecipient(entry)
            )
        }

        animatedComposable(FeedDestination) {
            Feed()
        }

        dialogComposable(GoToProfileConfirmationDestination) {
            GoToProfileConfirmation(
                resultNavigator = resultBackNavigator(
                    navController = navController,
                    destinationSpec = GoToProfileConfirmationDestination
                )
            )
        }

        animatedComposable(TestScreenDestination) { args, _ ->
            TestScreen(
                id = args.id,
                stuff1 = args.stuff1,
                stuff2 = args.stuff2,
                stuff3 = args.stuff3
            )
        }

        animatedComposable(ProfileScreenDestination) { _, entry ->
            val vm = viewModel<ProfileViewModel>(
                factory = ProfileViewModel.Factory(entry)
            )

            ProfileScreen(
                vm as ProfileUiState,
                vm as ProfileUiEvents
            )
        }

        navigation(
            startDestination = SettingsDestination.route,
            route = "settings"
        ) {
            animatedComposable(SettingsDestination) {
                Settings(
                    navigator = DestinationsNavController(navController, it),
                    themeSettingsResultRecipient = resultRecipient(it)
                )
            }

            bottomSheetComposable(ThemeSettingsDestination) {
                ThemeSettings(
                    resultNavigator = resultBackNavigator(
                        navController = navController,
                        destinationSpec = ThemeSettingsDestination
                    )
                )
            }
        }
    }
}