package com.ramcosta.composedestinations.result

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.ramcosta.composedestinations.spec.DestinationSpec
import com.ramcosta.composedestinations.spec.ExternalRoute
import kotlin.reflect.KClass

@Composable
@PublishedApi
internal fun <R, A : R & Any> resultBackNavigator(
    destination: DestinationSpec,
    resultType: KClass<A>,
    navController: NavController,
    navBackStackEntry: NavBackStackEntry
): ResultBackNavigator<R> {

    val backNavigator = remember(navController, navBackStackEntry, destination, resultType) {
        ResultBackNavigatorImpl<R, A>(
            navController = navController,
            resultOriginType = if (destination is ExternalRoute) {
                (destination.original as DestinationSpec)::class
            } else {
                destination::class
            },
            resultType = resultType
        )
    }

    backNavigator.handleCanceled()

    return backNavigator
}

@Composable
@PublishedApi
internal fun <D : DestinationSpec, R, A: R & Any> resultRecipient(
    navBackStackEntry: NavBackStackEntry,
    originType: KClass<D>,
    resultType: KClass<A>
): ResultRecipient<D, R> = remember(navBackStackEntry, originType, resultType) {
    ResultRecipientImpl(
        navBackStackEntry = navBackStackEntry,
        resultOriginType = originType,
        resultType = resultType,
    )
}

internal fun <D : DestinationSpec, R : Any> resultKey(
    resultOriginType: KClass<D>,
    resultType: KClass<R>
) = "compose-destinations@${resultOriginType.qualifiedName}@${resultType.qualifiedName}@result"

internal fun <D : DestinationSpec, R : Any> canceledKey(
    resultOriginType: KClass<D>,
    resultType: KClass<R>
) = "compose-destinations@${resultOriginType.qualifiedName}@${resultType.qualifiedName}@canceled"