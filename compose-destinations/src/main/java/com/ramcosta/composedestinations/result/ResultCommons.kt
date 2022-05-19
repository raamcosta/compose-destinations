package com.ramcosta.composedestinations.result

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.ramcosta.composedestinations.dynamic.originalDestination
import com.ramcosta.composedestinations.spec.DestinationSpec
import com.ramcosta.composedestinations.spec.DestinationStyle

@Composable
@PublishedApi
internal fun <R> resultBackNavigator(
    destination: DestinationSpec<*>,
    resultType: Class<R>,
    navController: NavController,
): ResultBackNavigator<R> {

    val backNavigator = remember {
        ResultBackNavigatorImpl(
            navController = navController,
            resultOriginType = destination.originalDestination.javaClass,
            resultType = resultType
        )
    }

    backNavigator.handleCanceled()

    return backNavigator
}

@Composable
@PublishedApi
internal fun <D : DestinationSpec<*>, R> resultRecipient(
    destination: DestinationSpec<*>,
    navBackStackEntry: NavBackStackEntry,
    originType: Class<D>,
    resultType: Class<R>
): ResultRecipient<D, R> = remember {
    ResultRecipientImpl(
        isRecipientDialogStyled = destination.style is DestinationStyle.Dialog,
        navBackStackEntry = navBackStackEntry,
        resultOriginType = originType,
        resultType = resultType,
    )
}

internal fun <D : DestinationSpec<*>, R> resultKey(
    resultOriginType: Class<D>,
    resultType: Class<R>
) = "compose-destinations@${resultOriginType.name}@${resultType.name}@result"

internal fun <D : DestinationSpec<*>, R> canceledKey(
    resultOriginType: Class<D>,
    resultType: Class<R>
) = "compose-destinations@${resultOriginType.name}@${resultType.name}@canceled"