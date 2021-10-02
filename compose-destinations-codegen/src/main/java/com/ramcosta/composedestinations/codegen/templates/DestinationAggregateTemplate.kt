package com.ramcosta.composedestinations.codegen.templates

import com.ramcosta.composedestinations.codegen.commons.*

//region anchors
internal const val IMPORTS_BLOCK = "[IMPORTS_BLOCK]"
internal const val NAV_GRAPHS_DECLARATION = "[NAV_GRAPHS_DECLARATION]"
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

/**
 * Class generated if any Composable is annotated with `@$DESTINATION_ANNOTATION`.
 * It aggregates all [$GENERATED_DESTINATION]s and has 
 * [DestinationsNavHost]/[DestinationsScaffold] equivalent methods which
 * will relay the destinations to be used in the navigation graph.
 */
object $DESTINATIONS_AGGREGATE_CLASS {
    $NAV_GRAPHS_DECLARATION

    /**
     * Like [DestinationsNavHost] but uses composables annotated with 
     * `@$DESTINATION_ANNOTATION` to pass in as the destinations available.
     * 
     * @see [DestinationsNavHost]
     */
    @Composable
    fun NavHost(
        navController: NavHostController,
        modifier: Modifier = Modifier,
        startDestination: $GENERATED_DESTINATION = ${GENERATED_NAV_GRAPH}s.root.startDestination,
        builder: NavGraphBuilder.() -> Unit = {}
    ) {
        DestinationsNavHost(
            ${GENERATED_NAV_GRAPH}s.root,
            navController,
            modifier,
            startDestination,
            { emptyMap() },
            builder
        )
    }

    /**
     * Like [DestinationsScaffold] but uses composables annotated with
     * `@$DESTINATION_ANNOTATION` to pass in as the destinations available.
     * It will also expose [Destination] as the generated sealed [$GENERATED_DESTINATION]
     * interface to allow for exhaustive when expressions.
     * 
     * @see [DestinationsScaffold]
     */
    @Composable
    fun Scaffold(
        modifier: Modifier = Modifier,
        startDestination: $GENERATED_DESTINATION = ${GENERATED_NAV_GRAPH}s.root.startDestination,
        navController: NavHostController = rememberNavController(),
        scaffoldState: ScaffoldState = rememberScaffoldState(),
        topBar: (@Composable ($GENERATED_DESTINATION) -> Unit) = {},
        bottomBar: @Composable ($GENERATED_DESTINATION) -> Unit = {},
        snackbarHost: @Composable (SnackbarHostState) -> Unit = { SnackbarHost(it) },
        floatingActionButton: @Composable ($GENERATED_DESTINATION) -> Unit = {},
        floatingActionButtonPosition: FabPosition = FabPosition.End,
        isFloatingActionButtonDocked: Boolean = false,
        drawerContent: @Composable (ColumnScope.($GENERATED_DESTINATION) -> Unit)? = null,
        drawerGesturesEnabled: Boolean = true,
        drawerShape: Shape = MaterialTheme.shapes.large,
        drawerElevation: Dp = DrawerDefaults.Elevation,
        drawerBackgroundColor: Color = MaterialTheme.colors.surface,
        drawerContentColor: Color = contentColorFor(drawerBackgroundColor),
        drawerScrimColor: Color = DrawerDefaults.scrimColor,
        backgroundColor: Color = MaterialTheme.colors.background,
        contentColor: Color = contentColorFor(backgroundColor),
        modifierForDestination: ($GENERATED_DESTINATION, PaddingValues) -> Modifier = { _, _ -> Modifier }
    ) {
        DestinationsScaffold(
            ${GENERATED_NAV_GRAPH}s.root,
            modifier,
            { emptyMap() },
            startDestination,
            navController,
            scaffoldState,
            { topBar(it as $GENERATED_DESTINATION) },
            { bottomBar(it as $GENERATED_DESTINATION) },
            snackbarHost,
            { floatingActionButton(it as $GENERATED_DESTINATION) },
            floatingActionButtonPosition,
            isFloatingActionButtonDocked,
            drawerContent?.let { { drawerContent.invoke(this, it as $GENERATED_DESTINATION) } },
            drawerGesturesEnabled,
            drawerShape,
            drawerElevation,
            drawerBackgroundColor,
            drawerContentColor,
            drawerScrimColor,
            backgroundColor,
            contentColor,
            { dest, padding -> modifierForDestination(dest as $GENERATED_DESTINATION, padding) }
        )
    }
}
""".trimIndent()