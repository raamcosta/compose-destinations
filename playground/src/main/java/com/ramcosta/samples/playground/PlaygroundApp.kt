package com.ramcosta.samples.playground

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.ramcosta.composedestinations.navigation.navigate
import com.ramcosta.samples.playground.commons.DrawerControllerImpl
import com.ramcosta.samples.playground.commons.composables.BottomBar
import com.ramcosta.samples.playground.commons.composables.PlaygroundScaffold
import com.ramcosta.samples.playground.commons.composables.MyDrawer
import com.ramcosta.samples.playground.commons.composables.TopBar
import com.ramcosta.samples.playground.ui.screens.NavGraphs
import com.ramcosta.samples.playground.ui.screens.settings.SettingsScreenNavArgs
import com.ramcosta.samples.playground.ui.theme.PlaygroundTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PlaygroundApp(testProfileDeepLink: () -> Unit) {
    PlaygroundTheme {
        val scaffoldState = rememberScaffoldState()
        val coroutineScope = rememberCoroutineScope()
        val navController = rememberAnimatedNavController()

        PlaygroundScaffold(
            scaffoldState = scaffoldState,
            navController = navController,
            topBar = { destination ->
                TopBar(
                    destination = destination,
                    onDrawerClick = { coroutineScope.launch { scaffoldState.drawerState.open() } },
                    onSettingsClick = { navController.navigate(NavGraphs.settings(SettingsScreenNavArgs(id = "123"))) }
                )
            },
            bottomBar = { destination ->
                BottomBar(
                    currentDestination = destination,
                    onBottomBarItemClick = {
                        navController.navigate(it) {
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
                testProfileDeepLink = testProfileDeepLink,
                drawerController = remember(scaffoldState) { DrawerControllerImpl(scaffoldState.drawerState) },
                navController = navController,
            )
        }
    }
}