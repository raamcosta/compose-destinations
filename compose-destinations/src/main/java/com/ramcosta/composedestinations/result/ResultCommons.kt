package com.ramcosta.composedestinations.result

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.ramcosta.composedestinations.navargs.DestinationsNavType
import com.ramcosta.composedestinations.spec.DestinationSpec
import com.ramcosta.composedestinations.spec.ExternalRoute
import kotlin.reflect.KClass

@Composable
@PublishedApi
internal fun <R> resultBackNavigator(
    destination: DestinationSpec,
    resultNavType: DestinationsNavType<in R>,
    navController: NavController,
    navBackStackEntry: NavBackStackEntry
): ResultBackNavigator<R> {

    val backNavigator = remember(navController, navBackStackEntry, destination, resultNavType) {
        ResultBackNavigatorImpl(
            navController = navController,
            navBackStackEntry = navBackStackEntry,
            resultOriginType = if (destination is ExternalRoute) {
                (destination.original as DestinationSpec)::class
            } else {
                destination::class
            },
            resultNavType = resultNavType
        )
    }

    backNavigator.handleCanceled()

    return backNavigator
}

@Composable
@PublishedApi
internal fun <D : DestinationSpec, R> resultRecipient(
    navBackStackEntry: NavBackStackEntry,
    originType: KClass<D>,
    resultNavType: DestinationsNavType<in R>,
): ResultRecipient<D, R> = remember(navBackStackEntry, originType, resultNavType) {
    ResultRecipientImpl(
        navBackStackEntry = navBackStackEntry,
        resultOriginType = originType,
        resultNavType = resultNavType,
    )
}

internal fun <D : DestinationSpec, R> resultKey(
    resultOriginType: KClass<D>,
    resultNavType: DestinationsNavType<R>
) = "compose-destinations@${resultOriginType.qualifiedName}@${resultNavType::class.qualifiedName}@result"

internal fun <D : DestinationSpec, R> canceledKey(
    resultOriginType: KClass<D>,
    resultNavType: DestinationsNavType<R>
) = "compose-destinations@${resultOriginType.qualifiedName}@${resultNavType::class.qualifiedName}@canceled"