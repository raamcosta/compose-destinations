package com.ramcosta.samples.playground

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.ramcosta.composedestinations.navigation.navigateTo
import com.ramcosta.samples.playground.commons.DrawerControllerImpl
import com.ramcosta.samples.playground.commons.composables.BottomBar
import com.ramcosta.samples.playground.commons.composables.DestinationsSampleScaffold
import com.ramcosta.samples.playground.commons.composables.MyDrawer
import com.ramcosta.samples.playground.commons.composables.TopBar
import com.ramcosta.samples.playground.ui.screens.NavGraphs
import com.ramcosta.samples.playground.ui.theme.DestinationsTodoSampleTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun DestinationsSampleApp() {
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
                BottomBar(
                    currentDestination = destination,
                    onBottomBarItemClick = {
                        navController.navigateTo(it) {
                            launchSingleTop = true
                        }
                    }
                )
            },
            drawerContent = { destination ->
                MyDrawer(
                    destination = destination,
                    navController = navController,
                    coroutineScope = coroutineScope,
                    scaffoldState = scaffoldState
                )
            },
        ) { paddingValues ->
            AppNavigation(
                modifier = Modifier.padding(paddingValues),
                drawerController = remember(scaffoldState) { DrawerControllerImpl(scaffoldState.drawerState) },
                navController = navController,
            )
        }
    }
}