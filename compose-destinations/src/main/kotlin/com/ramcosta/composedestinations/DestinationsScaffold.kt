package com.ramcosta.composedestinations

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

/**
 * Like [Scaffold] but it adds a navigation graph using [DestinationsScaffold]
 * with all [destinations] passed in as well as the [startDestination].
 *
 * Also, composables that can depend on the current [Destination] are
 * passed the current [Destination] so that they can update when the user
 * navigates through the app.
 *
 * Lastly, [modifierForPaddingValues] is used to let callers determine
 * a [Modifier] to set on the [DestinationsNavHost] for a combination
 * of [PaddingValues] (given from this [Scaffold]) and current [Destination].
 *
 * @see [Scaffold]
 */
@Composable
fun DestinationsScaffold(
    destinations: Map<String, Destination>,
    startDestination: Destination,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    topBar: (@Composable (Destination) -> Unit) = {},
    bottomBar: @Composable (Destination) -> Unit = {},
    snackbarHost: @Composable (SnackbarHostState) -> Unit = { SnackbarHost(it) },
    floatingActionButton: @Composable (Destination) -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    isFloatingActionButtonDocked: Boolean = false,
    drawerContent: @Composable (ColumnScope.(Destination) -> Unit)? = null,
    drawerGesturesEnabled: Boolean = true,
    drawerShape: Shape = MaterialTheme.shapes.large,
    drawerElevation: Dp = DrawerDefaults.Elevation,
    drawerBackgroundColor: Color = MaterialTheme.colors.surface,
    drawerContentColor: Color = contentColorFor(drawerBackgroundColor),
    drawerScrimColor: Color = DrawerDefaults.scrimColor,
    backgroundColor: Color = MaterialTheme.colors.background,
    contentColor: Color = contentColorFor(backgroundColor),
    modifierForPaddingValues: (Destination, PaddingValues) -> Modifier = { _, _ -> Modifier }
) {
    val currentBackStackEntryAsState by navController.currentBackStackEntryAsState()
    val destination = destinations[currentBackStackEntryAsState?.destination?.route] ?: startDestination

    Scaffold(
        modifier,
        scaffoldState,
        { topBar(destination) },
        { bottomBar(destination) },
        snackbarHost,
        { floatingActionButton(destination) },
        floatingActionButtonPosition,
        isFloatingActionButtonDocked,
        drawerContent?.let{ { drawerContent.invoke(this, destination) } },
        drawerGesturesEnabled,
        drawerShape,
        drawerElevation,
        drawerBackgroundColor,
        drawerContentColor,
        drawerScrimColor,
        backgroundColor,
        contentColor,
    ) { paddingValues ->
        DestinationsNavHost(
            modifier = modifierForPaddingValues(destination, paddingValues),
            destinations = destinations.values,
            navController = navController,
            startDestination = startDestination,
            scaffoldState = scaffoldState
        )
    }
}