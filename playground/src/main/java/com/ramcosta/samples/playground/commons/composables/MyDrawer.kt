package com.ramcosta.samples.playground.commons.composables

import androidx.compose.material.ScaffoldState
import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController
import com.ramcosta.composedestinations.navigation.navigate
import com.ramcosta.composedestinations.spec.DestinationSpec
import com.ramcosta.composedestinations.spec.DirectionDestinationSpec
import com.ramcosta.composedestinations.utils.destination
import com.ramcosta.composedestinations.utils.startDestination
import com.ramcosta.samples.playground.commons.DrawerContent
import com.ramcosta.samples.playground.ui.screens.NavGraphs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun MyDrawer(
    destination: DestinationSpec,
    navController: NavHostController,
    coroutineScope: CoroutineScope,
    scaffoldState: ScaffoldState
) {
    NavGraphs.root.destinations
        .filterIsInstance<DirectionDestinationSpec>()
        .sortedBy { if (it == NavGraphs.root.startRoute.startDestination) 0 else 1 }
        .forEach {
            it.DrawerContent(
                isSelected = it == destination,
                onDestinationClick = { clickedDestination ->
                    if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED
                        && navController.currentBackStackEntry?.destination() != clickedDestination
                    ) {
                        navController.navigate(clickedDestination)
                        coroutineScope.launch { scaffoldState.drawerState.close() }
                    }
                }
            )
        }
}