package com.ramcosta.samples.destinationstodosample

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.ramcosta.composedestinations.*
import com.ramcosta.composedestinations.animations.defaults.DefaultAnimationParams
import com.ramcosta.composedestinations.animations.rememberAnimatedNavHostEngine
import com.ramcosta.composedestinations.navigation.DestinationsNavController
import com.ramcosta.samples.destinationstodosample.commons.DrawerController
import com.ramcosta.samples.destinationstodosample.ui.screens.TestScreen
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
    val navHostEngine = rememberAnimatedNavHostEngine(DefaultAnimationParams.ACCOMPANIST_FADING)

    DestinationsNavHost(
        navGraph = NavGraphs.root,
        startDestination = if (Math.random() > 0.5) FeedDestination else NavGraphs.root.startDestination,
        engine = navHostEngine,
        navController = navController,
        modifier = modifier
    ) {
        //region This is NOT needed: this is exactly what the lib would do for us too
        // if we didn't explicitly call this Composable
        // It is here only as an example of getting nav args in a type safe way at this point
        // and also so we can see the boilerplate we save for each destination
        composable(TestScreenDestination) { navArgs, _ ->
            TestScreen(
                id = navArgs.id,
                stuff1 = navArgs.stuff1,
                stuff2 = navArgs.stuff2,
                stuff3 = navArgs.stuff3
            )
        }
        //endregion

        // Composables we need to call ourselves since the lib doesn't know how to get
        // DrawerController or the *UiState and *UiEvents interfaces
        animatedComposable(ProfileScreenDestination) { _, entry ->
            val vm = viewModel<ProfileViewModel>(
                factory = ProfileViewModel.Factory(entry)
            )

            ProfileScreen(
                vm as ProfileUiState,
                vm as ProfileUiEvents
            )
        }

        composable(GreetingScreenDestination) { entry ->
            val vm = viewModel<GreetingViewModel>()

            GreetingScreen(
                navigator = DestinationsNavController(navController, entry),
                drawerController = drawerController,
                uiEvents = vm as GreetingUiEvents,
                uiState = vm as GreetingUiState
            )
        }
    }
}