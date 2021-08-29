package com.ramcosta.composedestinations

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController


@Composable
fun DestinationsScaffold(
    destinations: Map<String, Destination>,
    startDestination: Destination,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    topBar: (@Composable () -> Unit)? = null,
    bottomBar: @Composable () -> Unit = {},
    snackbarHost: @Composable (SnackbarHostState) -> Unit = { SnackbarHost(it) },
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    isFloatingActionButtonDocked: Boolean = false,
    drawerContent: @Composable (ColumnScope.() -> Unit)? = null,
    drawerGesturesEnabled: Boolean = true,
    drawerShape: Shape = MaterialTheme.shapes.large,
    drawerElevation: Dp = DrawerDefaults.Elevation,
    drawerBackgroundColor: Color = MaterialTheme.colors.surface,
    drawerContentColor: Color = contentColorFor(drawerBackgroundColor),
    drawerScrimColor: Color = DrawerDefaults.scrimColor,
    backgroundColor: Color = MaterialTheme.colors.background,
    contentColor: Color = contentColorFor(backgroundColor),
) {
    val currentBackStackEntryAsState by navController.currentBackStackEntryAsState()

    Scaffold(
        modifier,
        scaffoldState,
        { topBar?.invoke() ?: DestinationsTopBar(destinations, currentBackStackEntryAsState, navController) },
        bottomBar,
        snackbarHost,
        floatingActionButton,
        floatingActionButtonPosition,
        isFloatingActionButtonDocked,
        drawerContent,
        drawerGesturesEnabled,
        drawerShape,
        drawerElevation,
        drawerBackgroundColor,
        drawerContentColor,
        drawerScrimColor,
        backgroundColor,
        contentColor,
    ) {
        DestinationsNavHost(
            destinations = destinations.values,
            navController = navController,
            startDestination = startDestination,
            scaffoldState = scaffoldState
        )
    }
}

@Composable
private fun DestinationsTopBar(
    destinations: Map<String, Destination>,
    currentBackStackEntry: NavBackStackEntry?,
    navController: NavHostController
) {

    if (currentBackStackEntry != null) {
        destinations[currentBackStackEntry.destination.route]!!.TopBar(
            navController = navController,
            navBackStackEntry = currentBackStackEntry
        )
    }
}