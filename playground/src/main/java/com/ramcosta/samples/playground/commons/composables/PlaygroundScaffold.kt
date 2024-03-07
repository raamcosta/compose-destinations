package com.ramcosta.samples.playground.commons.composables

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.navigation.ModalBottomSheetLayout
import androidx.compose.material.navigation.rememberBottomSheetNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.plusAssign
import com.ramcosta.composedestinations.utils.route
import com.ramcosta.samples.playground.ui.screens.*
import com.ramcosta.samples.playground.ui.screens.destinations.Destination

@SuppressLint("RestrictedApi")
@Composable
fun PlaygroundScaffold(
    navController: NavHostController,
    scaffoldState: ScaffoldState,
    topBar: @Composable (Destination) -> Unit,
    bottomBar: @Composable (Destination) -> Unit,
    drawerContent: @Composable ColumnScope.(Destination) -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    val destination = navController.appCurrentDestinationAsState().value
        ?: NavGraphs.root.startAppDestination

    //Just for me to debug, ignore this line
    navController.currentBackStack.collectAsState().value.print()

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

fun Collection<NavBackStackEntry>.print(prefix: String = "stack") {
    val stack = toMutableList()
        .map { it.route() }
        .filterIsInstance<Destination>()
        .map { it.javaClass.simpleName + "@" + it.toString().split("@")[1] }
        .toTypedArray().contentToString()
    println("$prefix = $stack")
}
