package com.ramcosta.samples.destinationstodosample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ScaffoldState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.ramcosta.composedestinations.*
import com.ramcosta.composedestinations.animations.defaults.DefaultAnimationParams
import com.ramcosta.composedestinations.animations.rememberAnimatedNavHostEngine
import com.ramcosta.composedestinations.navigation.DestinationsNavController
import com.ramcosta.composedestinations.navigation.navigateTo
import com.ramcosta.samples.destinationstodosample.destinations.*
import com.ramcosta.samples.destinationstodosample.destinations.commons.*
import com.ramcosta.samples.destinationstodosample.destinations.greeting.GreetingScreen
import com.ramcosta.samples.destinationstodosample.destinations.greeting.GreetingUiEvents
import com.ramcosta.samples.destinationstodosample.destinations.greeting.GreetingUiState
import com.ramcosta.samples.destinationstodosample.destinations.greeting.GreetingViewModel
import com.ramcosta.samples.destinationstodosample.destinations.profile.ProfileScreen
import com.ramcosta.samples.destinationstodosample.destinations.profile.ProfileUiEvents
import com.ramcosta.samples.destinationstodosample.destinations.profile.ProfileUiState
import com.ramcosta.samples.destinationstodosample.destinations.profile.ProfileViewModel
import com.ramcosta.samples.destinationstodosample.ui.theme.DestinationsTodoSampleTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@ExperimentalMaterialNavigationApi
@ExperimentalAnimationApi
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DestinationsTodoSampleTheme {
                val scaffoldState = rememberScaffoldState()
                val coroutineScope = rememberCoroutineScope()
                val navController = rememberAnimatedNavController()

                DestinationsSampleScaffold(
                    scaffoldState = scaffoldState,
                    navController = navController,
                    topBar = { destination ->
                        TopBar(
                            destination = destination,
                            onDrawerClick = { coroutineScope.launch { scaffoldState.drawerState.open() } },
                            onSettingsClick = { navController.navigateTo(NavGraphs.settings) }
                        )
                    },
                    bottomBar = { destination ->
                        BottomBar(destination)
                    },
                    drawerContent = { destination ->
                        Drawer(
                            destination = destination,
                            navController = navController,
                            coroutineScope = coroutineScope,
                            scaffoldState = scaffoldState
                        )
                    },
                ) { paddingValues ->
                    AppNavigation(
                        scaffoldState = scaffoldState,
                        navController = navController,
                        paddingValues = paddingValues
                    )
                }
            }
        }
    }

    @Composable
    private fun AppNavigation(
        scaffoldState: ScaffoldState,
        navController: NavHostController,
        paddingValues: PaddingValues
    ) {
        val drawerController: DrawerController =
            remember(scaffoldState) { DrawerControllerImpl(scaffoldState.drawerState) }

        DestinationsNavHost(
            engine = rememberAnimatedNavHostEngine(DefaultAnimationParams.ACCOMPANIST_FADING),
            navController = navController,
            navGraph = NavGraphs.root,
            startDestination = if (Math.random() > 0.5) FeedDestination else NavGraphs.root.startDestination,
            modifier = Modifier.padding(paddingValues)
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

    @Composable
    private fun Drawer(
        destination: Destination,
        navController: NavHostController,
        coroutineScope: CoroutineScope,
        scaffoldState: ScaffoldState
    ) {
        NavGraphs.root.destinations
            .sortedBy { if (it == NavGraphs.root.startDestination) 0 else 1 }
            .forEach {
                it.DrawerContent(
                    isSelected = it == destination,
                    onDestinationClick = { clickedDestination ->
                        if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED
                            && navController.currentBackStackEntry?.navDestination != clickedDestination
                        ) {
                            navController.navigate(clickedDestination.route)
                            coroutineScope.launch { scaffoldState.drawerState.close() }
                        }
                    }
                )
            }
    }
}