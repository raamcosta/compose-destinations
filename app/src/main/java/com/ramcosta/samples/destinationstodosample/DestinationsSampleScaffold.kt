package com.ramcosta.samples.destinationstodosample

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.plusAssign
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.ModalBottomSheetLayout
import com.google.accompanist.navigation.material.rememberBottomSheetNavigator
import com.ramcosta.composedestinations.Destination
import com.ramcosta.composedestinations.NavGraphs
import com.ramcosta.composedestinations.findDestination

@OptIn(ExperimentalMaterialNavigationApi::class, ExperimentalMaterialApi::class)
@Composable
fun DestinationsSampleScaffold(
    navController: NavHostController,
    scaffoldState: ScaffoldState,
    topBar: (@Composable (Destination) -> Unit),
    bottomBar: @Composable (Destination) -> Unit,
    drawerContent: @Composable ColumnScope.(Destination) -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {

    val currentBackStackEntryAsState by navController.currentBackStackEntryAsState()

    val destination = currentBackStackEntryAsState?.destination?.route
        ?.let { NavGraphs.root.findDestination(it) as Destination }
        ?: NavGraphs.root.startDestination

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
            drawerContent = { drawerContent.invoke(this, destination) },
            content = content
        )
    }
}