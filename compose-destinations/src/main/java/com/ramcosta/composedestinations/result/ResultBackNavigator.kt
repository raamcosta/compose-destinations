package com.ramcosta.composedestinations.result

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import com.ramcosta.composedestinations.spec.DestinationSpec

interface ResultBackNavigator<R> {

    fun navigateBack(result: R)
}

@Composable
inline fun <reified R> resultBackNavigator(
    navController: NavController,
    destinationSpec: DestinationSpec<*>
): ResultBackNavigator<R> = remember {
    ResultBackNavigatorImpl(
        navController = navController,
        resultOriginType = destinationSpec.javaClass,
        resultType = R::class.java
    )
}
