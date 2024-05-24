package com.ramcosta.composedestinations.result

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavBackStackEntry
import com.ramcosta.composedestinations.spec.DestinationSpec
import kotlin.reflect.KClass

internal class ResultRecipientImpl<D : DestinationSpec, R, A: R & Any>(
    private val navBackStackEntry: NavBackStackEntry,
    resultOriginType: KClass<D>,
    resultType: KClass<A>,
) : ResultRecipient<D, R> {

    private val resultKey = resultKey(resultOriginType, resultType)
    private val canceledKey = canceledKey(resultOriginType, resultType)

    @Composable
    override fun onNavResult(listener: (NavResult<R>) -> Unit) {
        val currentListener by rememberUpdatedState(listener)

        DisposableEffect(navBackStackEntry) {
            val observer = object : LifecycleEventObserver {
                override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                    when (event) {
                        Lifecycle.Event.ON_START,
                        Lifecycle.Event.ON_RESUME -> {
                            handleResultIfPresent(currentListener)
                        }

                        Lifecycle.Event.ON_DESTROY -> {
                            navBackStackEntry.lifecycle.removeObserver(this)
                        }

                        else -> Unit
                    }
                }
            }

            navBackStackEntry.lifecycle.addObserver(observer)

            onDispose {
                navBackStackEntry.lifecycle.removeObserver(observer)
            }
        }
    }

    private fun handleResultIfPresent(listener: (NavResult<R>) -> Unit) {
        if (!hasAnyResult()) {
            return
        }

        val canceled = navBackStackEntry.savedStateHandle.remove<Boolean>(canceledKey)

        if (canceled == true) {
            listener(NavResult.Canceled)
        } else if (navBackStackEntry.savedStateHandle.contains(resultKey)) {
            @Suppress("UNCHECKED_CAST")
            listener(
                NavResult.Value(
                    navBackStackEntry.savedStateHandle.remove<R>(resultKey) as R
                )
            )
        }
    }

    private fun hasAnyResult(): Boolean {
        return navBackStackEntry.savedStateHandle.contains(canceledKey) ||
                navBackStackEntry.savedStateHandle.contains(resultKey)
    }
}