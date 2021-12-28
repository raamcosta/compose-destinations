package com.ramcosta.samples.destinationstodosample.commons.composables

import androidx.compose.material.ScaffoldState
import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController
import com.ramcosta.composedestinations.destinations.Destination
import com.ramcosta.composedestinations.NavGraphs
import com.ramcosta.composedestinations.navDestination
import com.ramcosta.samples.destinationstodosample.commons.DrawerContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun MyDrawer(
    destination: Destination,
    navController: NavHostController,
    coroutineScope: CoroutineScope,
    scaffoldState: ScaffoldState
) {
    NavGraphs.root.destinations
        .sortedBy { if (it == NavGraphs.root.startDestination) 0 else 1 }
        .forEach {
            it.DrawerContent(
                isSelected = it == destination,
                onDestinationClick = { clickedDestination ->
                    if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED
                        && navController.currentBackStackEntry?.navDestination != clickedDestination
                    ) {
                        navController.navigate(clickedDestination.route)
                        coroutineScope.launch { scaffoldState.drawerState.close() }
                    }
                }
            )
        }
}