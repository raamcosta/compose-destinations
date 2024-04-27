package com.ramcosta.composedestinations.result

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.ramcosta.composedestinations.spec.DestinationSpec
import com.ramcosta.composedestinations.spec.ExternalRoute

@Composable
@PublishedApi
internal fun <R> resultBackNavigator(
    destination: DestinationSpec,
    resultType: Class<R>,
    navController: NavController,
    navBackStackEntry: NavBackStackEntry
): ResultBackNavigator<R> {

    val backNavigator = remember(navController, navBackStackEntry, destination, resultType) {
        ResultBackNavigatorImpl(
            navController = navController,
            resultOriginType = if (destination is ExternalRoute) {
                (destination.original as DestinationSpec).javaClass
            } else {
                destination.javaClass
            },
            resultType = resultType
        )
    }

    backNavigator.handleCanceled()

    return backNavigator
}

@Composable
@PublishedApi
internal fun <D : DestinationSpec, R> resultRecipient(
    navBackStackEntry: NavBackStackEntry,
    originType: Class<D>,
    resultType: Class<R>
): ResultRecipient<D, R> = remember(navBackStackEntry, originType, resultType) {
    ResultRecipientImpl(
        navBackStackEntry = navBackStackEntry,
        resultOriginType = originType,
        resultType = resultType,
    )
}

internal fun <D : DestinationSpec, R> resultKey(
    resultOriginType: Class<D>,
    resultType: Class<R>
) = "compose-destinations@${resultOriginType.name}@${resultType.name}@result"

internal fun <D : DestinationSpec, R> canceledKey(
    resultOriginType: Class<D>,
    resultType: Class<R>
) = "compose-destinations@${resultOriginType.name}@${resultType.name}@canceled"