package com.ramcosta.composedestinations

import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.PopUpToBuilder


fun NavController.navigateTo(
    destination: Destination,
    vararg args : Pair<String, Any>,
    builder: NavOptionsBuilder.() -> Unit
) {
    var route = destination.route

    args.forEach {
        route = route.replace("{${it.first}}", it.second.toString())
    }
    navigate(route, builder)
}

fun NavOptionsBuilder.popUpTo(
    destination: Destination,
    popUpToBuilder: PopUpToBuilder.() -> Unit = {}
) {
    popUpTo(destination.route, popUpToBuilder)
}