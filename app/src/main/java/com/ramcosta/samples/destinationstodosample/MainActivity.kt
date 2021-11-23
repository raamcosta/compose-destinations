package com.ramcosta.samples.destinationstodosample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ScaffoldState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.ramcosta.composedestinations.*
import com.ramcosta.composedestinations.navigation.DependenciesContainerBuilder
import com.ramcosta.composedestinations.navigation.dependency
import com.ramcosta.composedestinations.navigation.navigateTo
import com.ramcosta.composedestinations.spec.Routed
import com.ramcosta.samples.destinationstodosample.destinations.commons.*
import com.ramcosta.samples.destinationstodosample.destinations.greeting.GreetingUiEvents
import com.ramcosta.samples.destinationstodosample.destinations.greeting.GreetingUiState
import com.ramcosta.samples.destinationstodosample.destinations.greeting.GreetingViewModel
import com.ramcosta.samples.destinationstodosample.destinations.profile.ProfileUiEvents
import com.ramcosta.samples.destinationstodosample.destinations.profile.ProfileUiState
import com.ramcosta.samples.destinationstodosample.destinations.profile.ProfileViewModel
import com.ramcosta.samples.destinationstodosample.ui.theme.DestinationsTodoSampleTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@ExperimentalMaterialNavigationApi
@ExperimentalAnimationApi
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DestinationsTodoSampleTheme {
                val scaffoldState = rememberScaffoldState()
                val coroutineScope = rememberCoroutineScope()
                val navController = rememberDestinationsNavController()

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
                    val drawerController: DrawerController = remember(scaffoldState) { DrawerControllerImpl(scaffoldState.drawerState) }

                    DestinationsNavHost(
                        navController = navController,
                        startDestination = if (Math.random() > 0.5) FeedDestination else NavGraphs.root.startDestination,
                        defaultAnimationParams = DefaultAnimationParams.ACCOMPANIST_FADING,
                        modifier = Modifier.padding(paddingValues),
                        dependenciesContainerBuilder = { navBackStackEntry ->
                            dependency(drawerController)

                            AddDestinationDependencies(navBackStackEntry)
                        }
                    )
                }
            }
        }
    }

    @Composable
    private fun DependenciesContainerBuilder.AddDestinationDependencies(navBackStackEntry: NavBackStackEntry) {
        navBackStackEntry.navDestination?.let {
            when (it) {
                GreetingScreenDestination -> {
                    val vm = viewModel<GreetingViewModel>()
                    dependency<GreetingUiState>(vm)
                    dependency<GreetingUiEvents>(vm)
                }

                ProfileScreenDestination -> {
                    val vm = hiltViewModel<ProfileViewModel>()
                    dependency<ProfileUiState>(vm)
                    dependency<ProfileUiEvents>(vm)
                }

                else -> Unit /*no op*/
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
        NavGraphs.root.destinations.values
            .sortedBy { if (it == NavGraphs.root.startDestination) 0 else 1 }
            .forEach {
                it.DrawerContent(
                    isSelected = it == destination,
                    onDestinationClick = { clickedDestination ->
                        if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED
                            && navController.currentBackStackEntry?.navDestination != clickedDestination
                        ) {
                            navController.navigateTo(clickedDestination as Routed)
                            coroutineScope.launch { scaffoldState.drawerState.close() }
                        }
                    }
                )
            }
    }
}