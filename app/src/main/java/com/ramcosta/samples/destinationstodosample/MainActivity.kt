package com.ramcosta.samples.destinationstodosample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.compose.rememberNavController
import com.ramcosta.composedestinations.Destinations
import com.ramcosta.samples.destinationstodosample.ui.theme.DestinationsTodoSampleTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DestinationsTodoSampleTheme {
                val scaffoldState = rememberScaffoldState()
                val coroutineScope = rememberCoroutineScope()
                val navController = rememberNavController()

                Destinations.Scaffold(
                    scaffoldState = scaffoldState,
                    navController = navController,
                    topBar = {
                        MyTopBar(
                            destination = it,
                            onDrawerClick = { coroutineScope.launch { scaffoldState.drawerState.open() } }
                        )
                    },
                    bottomBar = {
                        MyBottomBar(destination = it)
                    },
                    drawerContent = { currentDestination ->
                        Destinations.all.values
                            .sortedBy { if (it == Destinations.start) 0 else 1 }
                            .forEach {
                                it.DrawerContent(
                                    isSelected = it == currentDestination,
                                    onDestinationClick = { clickedDestination ->
                                        navController.navigate(clickedDestination.route)
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