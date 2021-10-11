package com.ramcosta.samples.destinationstodosample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.dp
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.ramcosta.composedestinations.DefaultAnimationParams
import com.ramcosta.composedestinations.Destinations
import com.ramcosta.composedestinations.FeedDestination
import com.ramcosta.composedestinations.navigation.navigateTo
import com.ramcosta.samples.destinationstodosample.ui.theme.DestinationsTodoSampleTheme
import kotlinx.coroutines.launch

@ExperimentalMaterialNavigationApi
@ExperimentalAnimationApi
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DestinationsTodoSampleTheme {
                val scaffoldState = rememberScaffoldState()
                val coroutineScope = rememberCoroutineScope()
//                val navController = rememberNavController()
                val navController = rememberAnimatedNavController()

                Destinations.Scaffold(
                    scaffoldState = scaffoldState,
                    navController = navController,
                    bottomSheetParams = {
                        sheetShape(RoundedCornerShape(16.dp))
                    },
                    defaultAnimationParams = DefaultAnimationParams.ACCOMPANIST_FADING,
                    startDestination = if (Math.random() > 0.5) FeedDestination else Destinations.NavGraphs.root.startDestination,
                    topBar = {
                        MyTopBar(
                            destination = it,
                            onDrawerClick = { coroutineScope.launch { scaffoldState.drawerState.open() } },
                            onSettingsClick = { navController.navigateTo(Destinations.NavGraphs.settings) }
                        )
                    },
                    bottomBar = {
                        MyBottomBar(destination = it)
                    },
                    drawerContent = { currentDestination ->
                        Destinations.NavGraphs.root.destinations.values
                            .sortedBy { if (it == Destinations.NavGraphs.root.startDestination) 0 else 1 }
                            .forEach {
                                it.DrawerContent(
                                    isSelected = it == currentDestination,
                                    onDestinationClick = { clickedDestination ->
                                        navController.navigateTo(clickedDestination)
                                        coroutineScope.launch { scaffoldState.drawerState.close() }
                                    }
                                )
                        }
                    },
                    modifierForDestination = { destination, padding ->
                        destination.destinationPadding(parentPadding = padding)
                    }
                )
            }
        }
    }
}