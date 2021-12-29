package com.ramcosta.samples.destinationstodosample

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.ramcosta.samples.destinationstodosample.ui.screens.NavGraphs
import com.ramcosta.composedestinations.navigation.navigateTo
import com.ramcosta.samples.destinationstodosample.commons.*
import com.ramcosta.samples.destinationstodosample.commons.composables.BottomBar
import com.ramcosta.samples.destinationstodosample.commons.composables.DestinationsSampleScaffold
import com.ramcosta.samples.destinationstodosample.commons.composables.MyDrawer
import com.ramcosta.samples.destinationstodosample.commons.composables.TopBar
import com.ramcosta.samples.destinationstodosample.ui.theme.DestinationsTodoSampleTheme
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
                BottomBar(destination)
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