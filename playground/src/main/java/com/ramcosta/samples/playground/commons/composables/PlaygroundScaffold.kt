package com.ramcosta.samples.playground.commons.composables

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.plusAssign
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.ModalBottomSheetLayout
import com.google.accompanist.navigation.material.rememberBottomSheetNavigator
import com.ramcosta.samples.playground.ui.screens.NavGraphs
import com.ramcosta.samples.playground.ui.screens.appCurrentDestinationAsState
import com.ramcosta.samples.playground.ui.screens.appDestination
import com.ramcosta.samples.playground.ui.screens.destinations.Destination

@OptIn(ExperimentalMaterialNavigationApi::class, ExperimentalMaterialApi::class)
@Composable
fun PlaygroundScaffold(
    navController: NavHostController,
    scaffoldState: ScaffoldState,
    topBar: @Composable (Destination) -> Unit,
    bottomBar: @Composable (Destination) -> Unit,
    drawerContent: @Composable ColumnScope.(Destination) -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    val destination by navController.appCurrentDestinationAsState()

    //Just for me to debug, ignore this line
    navController.backQueue.print()

    val bottomSheetNavigator = rememberBottomSheetNavigator()
    navController.navigatorProvider += bottomSheetNavigator

    ModalBottomSheetLayout(
        bottomSheetNavigator = bottomSheetNavigator,
        sheetShape = RoundedCornerShape(16.dp)
    ) {
        Scaffold(
            scaffoldState = scaffoldState,
            topBar = { destination?.let { topBar(it) } },
            bottomBar = { destination?.let { bottomBar(it) } },
            drawerContent = { destination?.let { drawerContent(it) } },
            content = content
        )
    }
}

fun ArrayDeque<NavBackStackEntry>.print(prefix: String = "stack") {
    val stack = toMutableList()
        .filter { it.destination.route !in listOf(NavGraphs.root.route, NavGraphs.settings.route) }
        .map { it.appDestination().javaClass.simpleName + "@" + it.toString().split("@")[1] }
        .toTypedArray().contentToString()
    println("$prefix = $stack")
}
