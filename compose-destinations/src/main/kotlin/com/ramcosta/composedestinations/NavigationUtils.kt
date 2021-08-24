package com.ramcosta.composedestinations

import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.PopUpToBuilder


fun NavController.navigateTo(
    screenDestination: ScreenDestination,
    vararg args : Pair<String, Any>,
    builder: NavOptionsBuilder.() -> Unit
) {
    var route = screenDestination.route

    args.forEach {
        route = route.replace("{${it.first}}", it.second.toString())
    }
    navigate(route, builder)
}

fun NavOptionsBuilder.popUpTo(
    screenDestination: ScreenDestination,
    popUpToBuilder: PopUpToBuilder.() -> Unit = {}
) {
    popUpTo(screenDestination.route, popUpToBuilder)
}