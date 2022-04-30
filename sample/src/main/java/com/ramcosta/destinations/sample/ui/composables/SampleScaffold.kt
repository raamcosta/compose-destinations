package com.ramcosta.destinations.sample.ui.composables

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.plusAssign
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.ModalBottomSheetLayout
import com.google.accompanist.navigation.material.rememberBottomSheetNavigator
import com.ramcosta.destinations.sample.NavGraphs
import com.ramcosta.destinations.sample.appCurrentDestinationAsState
import com.ramcosta.destinations.sample.destinations.Destination
import com.ramcosta.destinations.sample.startAppDestination

@OptIn(ExperimentalMaterialNavigationApi::class)
@Composable
fun SampleScaffold(
    navController: NavHostController,
    topBar: @Composable (Destination) -> Unit,
    bottomBar: @Composable (Destination) -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    val destination = navController.appCurrentDestinationAsState().value
        ?: NavGraphs.root.startAppDestination

    val bottomSheetNavigator = rememberBottomSheetNavigator()
    navController.navigatorProvider += bottomSheetNavigator

    ModalBottomSheetLayout(
        bottomSheetNavigator = bottomSheetNavigator,
        sheetShape = RoundedCornerShape(16.dp)
    ) {
        Scaffold(
            topBar = { topBar(destination) },
            bottomBar = { bottomBar(destination) },
            content = content
        )
    }
}
