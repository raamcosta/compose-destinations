package com.ramcosta.composedestinations.templates

import com.ramcosta.composedestinations.utils.DESTINATIONS_AGGREGATE_CLASS
import com.ramcosta.composedestinations.utils.DESTINATION_DEFINITION
import com.ramcosta.composedestinations.utils.PACKAGE_NAME

//region anchors
internal const val IMPORTS_BLOCK = "[IMPORTS_BLOCK]"
internal const val DESTINATIONS_COUNT = "[DESTINATIONS_COUNT]"
internal const val DESTINATIONS_INSIDE_MAP_OF = "[DESTINATIONS_INSIDE_MAP_OF]"
//endregion

internal val destinationsTemplate = """
package $PACKAGE_NAME

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
$IMPORTS_BLOCK

object $DESTINATIONS_AGGREGATE_CLASS {

    val count = $DESTINATIONS_COUNT

    // destinations by route
    val all: Map<String, $DESTINATION_DEFINITION> = mapOf(
        $DESTINATIONS_INSIDE_MAP_OF
    )

    @Composable
    fun NavHost(
        navController: NavHostController,
        startDestination: Destination,
        modifier: Modifier = Modifier,
        route: String? = null,
        builder: NavGraphBuilder.() -> Unit = {}
    ) {
        DestinationsNavHost(
            all.values,
            navController,
            startDestination,
            modifier,
            route,
            builder
        )
    }

    @Composable
    fun Scaffold(
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
        DestinationsScaffold(
            all,
            startDestination,
            modifier,
            navController,
            scaffoldState,
            topBar,
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
            contentColor
        )
    }
}
""".trimIndent()