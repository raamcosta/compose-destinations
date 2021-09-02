package com.ramcosta.composedestinations.codegen.templates

import com.ramcosta.composedestinations.codegen.commons.DESTINATIONS_AGGREGATE_CLASS
import com.ramcosta.composedestinations.codegen.commons.DESTINATION_SPEC
import com.ramcosta.composedestinations.codegen.commons.PACKAGE_NAME

//region anchors
internal const val IMPORTS_BLOCK = "[IMPORTS_BLOCK]"
internal const val DESTINATIONS_COUNT = "[DESTINATIONS_COUNT]"
internal const val DESTINATIONS_INSIDE_MAP_OF = "[DESTINATIONS_INSIDE_MAP_OF]"
internal const val STARTING_DESTINATION = "[STARTING_DESTINATION]"
//endregion

internal val destinationsTemplate = """
package $PACKAGE_NAME

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
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

sealed interface $DESTINATION_SPEC : Destination

object $DESTINATIONS_AGGREGATE_CLASS {

    val count: Int = $DESTINATIONS_COUNT
    
    val start: $DESTINATION_SPEC = $STARTING_DESTINATION

    // destinations by route
    val all: Map<String, $DESTINATION_SPEC> = mapOf(
        $DESTINATIONS_INSIDE_MAP_OF
    )

    @Composable
    fun NavHost(
        navController: NavHostController,
        modifier: Modifier = Modifier,
        route: String? = null,
        builder: NavGraphBuilder.() -> Unit = {}
    ) {
        DestinationsNavHost(
            all.values,
            navController,
            start,
            modifier,
            null,
            route,
            builder
        )
    }

    @Composable
    fun Scaffold(
        modifier: Modifier = Modifier,
        navController: NavHostController = rememberNavController(),
        scaffoldState: ScaffoldState = rememberScaffoldState(),
        topBar: (@Composable ($DESTINATION_SPEC) -> Unit) = {},
        bottomBar: @Composable ($DESTINATION_SPEC) -> Unit = {},
        snackbarHost: @Composable (SnackbarHostState) -> Unit = { SnackbarHost(it) },
        floatingActionButton: @Composable ($DESTINATION_SPEC) -> Unit = {},
        floatingActionButtonPosition: FabPosition = FabPosition.End,
        isFloatingActionButtonDocked: Boolean = false,
        drawerContent: @Composable (ColumnScope.($DESTINATION_SPEC) -> Unit)? = null,
        drawerGesturesEnabled: Boolean = true,
        drawerShape: Shape = MaterialTheme.shapes.large,
        drawerElevation: Dp = DrawerDefaults.Elevation,
        drawerBackgroundColor: Color = MaterialTheme.colors.surface,
        drawerContentColor: Color = contentColorFor(drawerBackgroundColor),
        drawerScrimColor: Color = DrawerDefaults.scrimColor,
        backgroundColor: Color = MaterialTheme.colors.background,
        contentColor: Color = contentColorFor(backgroundColor),
        modifierForDestination: ($DESTINATION_SPEC, PaddingValues) -> Modifier = { _, _ -> Modifier }
    ) {
        DestinationsScaffold(
            all,
            start,
            modifier,
            navController,
            scaffoldState,
            { topBar(it as $DESTINATION_SPEC) },
            { bottomBar(it as $DESTINATION_SPEC) },
            snackbarHost,
            { floatingActionButton(it as $DESTINATION_SPEC) },
            floatingActionButtonPosition,
            isFloatingActionButtonDocked,
            drawerContent?.let { { drawerContent.invoke(this, it as $DESTINATION_SPEC) } },
            drawerGesturesEnabled,
            drawerShape,
            drawerElevation,
            drawerBackgroundColor,
            drawerContentColor,
            drawerScrimColor,
            backgroundColor,
            contentColor,
            { dest, padding -> modifierForDestination(dest as $DESTINATION_SPEC, padding) }
        )
    }
}
""".trimIndent()