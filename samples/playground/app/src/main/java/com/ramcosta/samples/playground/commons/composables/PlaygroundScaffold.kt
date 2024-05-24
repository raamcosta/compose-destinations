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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.plusAssign
import com.ramcosta.composedestinations.spec.DestinationSpec
import com.ramcosta.composedestinations.utils.currentDestinationAsState
import com.ramcosta.composedestinations.utils.route
import com.ramcosta.composedestinations.utils.startDestination
import com.ramcosta.samples.playground.ui.screens.NavGraphs

@Composable
fun PlaygroundScaffold(
    navController: NavHostController,
    scaffoldState: ScaffoldState,
    topBar: @Composable (DestinationSpec) -> Unit,
    bottomBar: @Composable (DestinationSpec) -> Unit,
    drawerContent: @Composable ColumnScope.(DestinationSpec) -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    val destination = navController.currentDestinationAsState().value
        ?: NavGraphs.root.startDestination

    //Just for me to debug, ignore this line
    LogBackStack(navController)

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

@SuppressLint("RestrictedApi")
@Composable
fun LogBackStack(navController: NavController) {
    LaunchedEffect(navController) {
        navController.currentBackStack.collect {
            it.print()
        }
    }
}

fun Collection<NavBackStackEntry>.print(prefix: String = "stack") {
    val stack = toMutableList()
        .map {
            val route = it.route()
            val args = runCatching { route.argsFrom(it) }.getOrNull()?.takeIf { it != Unit }?.let { "(args={$it})" } ?: ""
            "$route$args"
        }
        .toTypedArray().contentToString()
    println("$prefix = $stack")
}
