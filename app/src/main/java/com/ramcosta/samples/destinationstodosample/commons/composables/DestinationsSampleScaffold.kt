package com.ramcosta.samples.destinationstodosample.commons.composables

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.ramcosta.samples.destinationstodosample.ui.screens.NavGraphs
import com.ramcosta.samples.destinationstodosample.ui.screens.destinations.Destination
import com.ramcosta.samples.destinationstodosample.ui.screens.navDestination

@Composable
fun DestinationsSampleScaffold(
    navController: NavHostController,
    scaffoldState: ScaffoldState,
    topBar: @Composable (Destination) -> Unit,
    bottomBar: @Composable (Destination) -> Unit,
    drawerContent: @Composable ColumnScope.(Destination) -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    val currentBackStackEntryAsState by navController.currentBackStackEntryAsState()
    val destination = currentBackStackEntryAsState?.navDestination ?: NavGraphs.root.startDestination

    navController.backQueue.print()

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = { topBar(destination) },
        bottomBar = { bottomBar(destination) },
        drawerContent = { drawerContent(destination) },
        content = content
    )
}

fun ArrayDeque<NavBackStackEntry>.print(prefix: String = "stack") {
    val stack = toMutableList()
        .filter { it.destination.route !in listOf(NavGraphs.root.route, NavGraphs.settings.route) }
        .map { it.navDestination?.javaClass?.simpleName + "@" + it.toString().split("@")[1] }
        .toTypedArray().contentToString()
    println("$prefix = $stack")
}
