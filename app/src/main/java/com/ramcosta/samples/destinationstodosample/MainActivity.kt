package com.ramcosta.samples.destinationstodosample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ScaffoldState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController
//import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.ramcosta.composedestinations.*
import com.ramcosta.composedestinations.navigation.navigateTo
import com.ramcosta.samples.destinationstodosample.destinations.commons.DrawerController
import com.ramcosta.samples.destinationstodosample.destinations.commons.DrawerControllerImpl
import com.ramcosta.samples.destinationstodosample.ui.theme.DestinationsTodoSampleTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

//@ExperimentalMaterialNavigationApi
@ExperimentalAnimationApi
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
                        destination.TopBar(
                            onDrawerClick = { coroutineScope.launch { scaffoldState.drawerState.open() } },
                            onSettingsClick = { navController.navigateTo(NavGraphs.settings) }
                        )
                    },
                    bottomBar = { destination ->
                        destination.BottomBar()
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
                    DestinationsNavHost(
                        navController = navController,
                        startDestination = if (Math.random() > 0.5) FeedDestination else NavGraphs.root.startDestination,
//                        defaultAnimationParams = DefaultAnimationParams.ACCOMPANIST_FADING,
                        modifier = Modifier.padding(paddingValues),
                        destinationDependencies = mapOf(
                            DrawerController::class.java to DrawerControllerImpl(scaffoldState.drawerState)
                        )
                    )
                }
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
                            navController.navigateTo(clickedDestination)
                            coroutineScope.launch { scaffoldState.drawerState.close() }
                        }
                    }
                )
            }
    }
}