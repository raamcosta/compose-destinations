@file:SuppressLint("ComposableNaming")
@file:Suppress("UNCHECKED_CAST")

package com.ramcosta.composedestinations.result

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavBackStackEntry
import com.ramcosta.composedestinations.spec.DestinationSpec

interface ResultRecipient<D : DestinationSpec<*>, R> {

    @Composable
    fun onResult(lambda: (R) -> Unit)

}

@Composable
inline fun <reified D : DestinationSpec<*>, reified R> resultRecipient(
    navBackStackEntry: NavBackStackEntry
): ResultRecipient<D, R> = remember {
    ResultRecipientImpl(
        navBackStackEntry = navBackStackEntry,
        resultOriginType = D::class.java,
        resultType = R::class.java,
    )
}
