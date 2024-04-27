package com.ramcosta.samples.playground

import androidx.compose.foundation.layout.padding
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.ramcosta.composedestinations.utils.rememberDestinationsNavigator
import com.ramcosta.samples.playground.commons.DrawerControllerImpl
import com.ramcosta.samples.playground.commons.composables.BottomBar
import com.ramcosta.samples.playground.commons.composables.MyDrawer
import com.ramcosta.samples.playground.commons.composables.PlaygroundScaffold
import com.ramcosta.samples.playground.commons.composables.TopBar
import com.ramcosta.samples.playground.ui.screens.NavGraphs
import com.ramcosta.samples.playground.ui.theme.PlaygroundTheme
import kotlinx.coroutines.launch

@Composable
fun PlaygroundApp(testProfileDeepLink: () -> Unit) {
    PlaygroundTheme {
        val scaffoldState = rememberScaffoldState()
        val coroutineScope = rememberCoroutineScope()
        val navController = rememberNavController()
        val navigator = navController.rememberDestinationsNavigator()

        PlaygroundScaffold(
            scaffoldState = scaffoldState,
            navController = navController,
            topBar = { destination ->
                TopBar(
                    destination = destination,
                    onDrawerClick = { coroutineScope.launch { scaffoldState.drawerState.open() } },
                    onSettingsClick = { navigator.navigate(NavGraphs.settings) }
                )
            },
            bottomBar = { destination ->
                BottomBar(
                    currentDestination = destination,
                    onBottomBarItemClick = {
                        navigator.navigate(it) {
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