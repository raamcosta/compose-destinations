package com.ramcosta.composedestinations

import androidx.compose.runtime.Composable
import androidx.navigation.*
import androidx.navigation.compose.NamedNavArgument

interface ScreenDestination {

    val route: String

    val arguments: List<NamedNavArgument> get() = emptyList()

    @Composable
    fun TopBar(navController: NavController, navBackStackEntry: NavBackStackEntry) {

    }

    @Composable
    fun Content(navController: NavController, navBackStackEntry: NavBackStackEntry)
}