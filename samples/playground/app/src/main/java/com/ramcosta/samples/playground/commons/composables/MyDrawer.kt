package com.ramcosta.samples.playground.commons.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.spec.DestinationSpec
import com.ramcosta.composedestinations.spec.DirectionDestinationSpec
import com.ramcosta.composedestinations.utils.startDestination
import com.ramcosta.samples.playground.commons.DrawerContent
import com.ramcosta.samples.playground.ui.screens.NavGraphs
import com.ramcosta.samples.playground.ui.screens.destinations.ProfileSettingsProfileSettingsScreenDestination
import com.ramcosta.samples.playground.ui.screens.destinations.RootProfileSettingsScreenDestination
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun MyDrawer(
    destination: DestinationSpec,
    navigator: DestinationsNavigator,
    coroutineScope: CoroutineScope,
    scaffoldState: ScaffoldState
) = Column(Modifier.fillMaxSize()){
    NavGraphs.root.destinations
        .filterIsInstance<DirectionDestinationSpec>()
        .sortedBy { if (it == NavGraphs.root.startRoute.startDestination) 0 else 1 }
        .forEach {
            it.DrawerContent(
                isSelected = it == destination,
                onDestinationClick = { clickedDestination ->
                    navigator.navigate(clickedDestination as DirectionDestinationSpec)
                    coroutineScope.launch { scaffoldState.drawerState.close() }
                }
            )
        }

    ProfileSettingsProfileSettingsScreenDestination.DrawerContent(
        isSelected = destination == ProfileSettingsProfileSettingsScreenDestination,
        onDestinationClick = {
            navigator.navigate(ProfileSettingsProfileSettingsScreenDestination(true))
        }
    )

    RootProfileSettingsScreenDestination.DrawerContent(
        isSelected = destination == RootProfileSettingsScreenDestination,
        onDestinationClick = {
            navigator.navigate(RootProfileSettingsScreenDestination(false))
        }
    )
}