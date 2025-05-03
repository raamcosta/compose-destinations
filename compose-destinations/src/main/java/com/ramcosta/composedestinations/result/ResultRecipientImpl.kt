@file:SuppressLint("ComposableNaming")
@file:Suppress("UNCHECKED_CAST")

package com.ramcosta.composedestinations.result

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavBackStackEntry
import com.ramcosta.composedestinations.navargs.DestinationsNavType
import com.ramcosta.composedestinations.spec.DestinationSpec
import kotlin.reflect.KClass

internal class ResultRecipientImpl<D : DestinationSpec, R>(
    private val navBackStackEntry: NavBackStackEntry,
    resultOriginType: KClass<D>,
    private val resultNavType: DestinationsNavType<in R>,
) : ResultRecipient<D, R> {

    private val resultKey = resultKey(resultOriginType, resultNavType)
    private val canceledKey = canceledKey(resultOriginType, resultNavType)

    @Composable
    override fun onNavResult(listener: (NavResult<R>) -> Unit) {
        onNavResult(
            deliverResultOn = OpenResultRecipient.DeliverResultOn.FIRST_OPPORTUNITY,
            listener = listener
        )
    }

    @Composable
    override fun onNavResult(
        deliverResultOn: OpenResultRecipient.DeliverResultOn,
        listener: (NavResult<R>) -> Unit
    ) {
        val currentListener by rememberUpdatedState(listener)

        DisposableEffect(navBackStackEntry) {
            val observer = object : LifecycleEventObserver {
                override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                    when (event) {
                        Lifecycle.Event.ON_START -> {
                            if (deliverResultOn == OpenResultRecipient.DeliverResultOn.START ||
                                deliverResultOn == OpenResultRecipient.DeliverResultOn.FIRST_OPPORTUNITY
                            ) {
                                handleResultIfPresent(currentListener)
                            }
                        }

                        Lifecycle.Event.ON_RESUME -> {
                            if (deliverResultOn == OpenResultRecipient.DeliverResultOn.RESUME ||
                                deliverResultOn == OpenResultRecipient.DeliverResultOn.FIRST_OPPORTUNITY
                            ) {
                                handleResultIfPresent(currentListener)
                            }
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
            val result = resultNavType.get(navBackStackEntry.savedStateHandle, resultKey) as R
            navBackStackEntry.savedStateHandle.remove<Any?>(resultKey)
            listener(NavResult.Value(result))
        }
    }

    private fun hasAnyResult(): Boolean {
        return navBackStackEntry.savedStateHandle.contains(canceledKey) ||
                navBackStackEntry.savedStateHandle.contains(resultKey)
    }
}