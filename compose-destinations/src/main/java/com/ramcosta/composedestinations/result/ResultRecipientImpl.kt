@file:SuppressLint("ComposableNaming")
@file:Suppress("UNCHECKED_CAST")

package com.ramcosta.composedestinations.result

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavBackStackEntry
import com.ramcosta.composedestinations.spec.DestinationSpec

/**
 * Internal details, public only for inline functions.
 *
 *  @see [ResultRecipient].
 */
class ResultRecipientImpl<D : DestinationSpec<*>, R>(
    private val navBackStackEntry: NavBackStackEntry,
    resultOriginType: Class<D>,
    resultType: Class<R>,
) : ResultRecipient<D, R> {

    private val resultKey = resultKey(resultOriginType, resultType)

    @Composable
    override fun onResult(listener: (R) -> Unit) {
        DisposableEffect(key1 = Unit) {
            navBackStackEntry.lifecycle.addObserver(object : LifecycleEventObserver {
                override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                    when (event) {
                        Lifecycle.Event.ON_RESUME -> {
                            if (navBackStackEntry.savedStateHandle.contains(resultKey)) {
                                listener(navBackStackEntry.savedStateHandle.remove<R>(resultKey) as R)
                            }
                        }

                        Lifecycle.Event.ON_DESTROY -> {
                            navBackStackEntry.lifecycle.removeObserver(this)
                        }

                        else -> Unit
                    }
                }
            })

            onDispose { }
        }
    }
}