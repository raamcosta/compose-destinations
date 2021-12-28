package com.ramcosta.samples.destinationstodosample.commons.composables

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
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.plusAssign
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.ModalBottomSheetLayout
import com.google.accompanist.navigation.material.rememberBottomSheetNavigator
import com.ramcosta.composedestinations.destinations.Destination
import com.ramcosta.composedestinations.NavGraphs
import com.ramcosta.composedestinations.navDestination

fun ArrayDeque<NavBackStackEntry>.print(prefix: String = "stack") {
    val stack = toMutableList()
        .filter { it.destination.route !in listOf(NavGraphs.root.route, NavGraphs.settings.route) }
        .map { it.navDestination?.javaClass?.simpleName + "@" + it.toString().split("@")[1] }
        .toTypedArray().contentToString()
    println("$prefix = $stack")
}

@OptIn(ExperimentalMaterialNavigationApi::class, ExperimentalMaterialApi::class)
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

    val bottomSheetNavigator = rememberBottomSheetNavigator()
    navController.navigatorProvider += bottomSheetNavigator

    ModalBottomSheetLayout(
        bottomSheetNavigator = bottomSheetNavigator,
        sheetShape = RoundedCornerShape(16.dp)
    ) {
        Scaffold(
            scaffoldState = scaffoldState,
            topBar = { topBar(destination) },
            bottomBar = { bottomBar(destination) },
            drawerContent = { drawerContent(destination) },
            content = content
        )
    }
}