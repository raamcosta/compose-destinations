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
import com.ramcosta.composedestinations.spec.DestinationSpec

/**
 * Internal details, public only for inline functions.
 *
 *  @see [ResultRecipient].
 */
class ResultRecipientImpl<D : DestinationSpec<*>, R>(
    private val isRecipientDialogStyled: Boolean,
    private val navBackStackEntry: NavBackStackEntry,
    resultOriginType: Class<D>,
    resultType: Class<R>,
) : ResultRecipient<D, R> {

    private val resultKey = resultKey(resultOriginType, resultType)
    private val canceledKey = canceledKey(resultOriginType, resultType)

    @Composable
    override fun onNavResult(listener: (NavResult<R>) -> Unit) {
        val currentListener by rememberUpdatedState(listener)

        DisposableEffect(key1 = Unit) {
            val observer = object : LifecycleEventObserver {
                override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                    when (event) {
                        Lifecycle.Event.ON_START -> {
                            // If the destination listening is a Dialog, then we need to consider
                            // ON_START as well, since moving between Dialogs, don't trigger ON_RESUME
                            if (isRecipientDialogStyled && hasAnyResult()) {
                                handleResult(currentListener)
                            }
                        }
                        Lifecycle.Event.ON_RESUME -> {
                            handleResult(currentListener)
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

    private fun hasAnyResult(): Boolean {
        return navBackStackEntry.savedStateHandle.contains(canceledKey) ||
                navBackStackEntry.savedStateHandle.contains(resultKey)
    }

    private fun handleResult(currentListener: (NavResult<R>) -> Unit) {
        val canceled = navBackStackEntry.savedStateHandle.remove<Boolean>(canceledKey)

        if (canceled == true) {
            currentListener(NavResult.Canceled)
        } else if (navBackStackEntry.savedStateHandle.contains(resultKey)) {
            currentListener(
                NavResult.Value(
                    navBackStackEntry.savedStateHandle.remove<R>(resultKey) as R
                )
            )
        }
    }

    @Suppress("OverridingDeprecatedMember")
    @Composable
    override fun onResult(listener: (R) -> Unit) {
        val currentListener by rememberUpdatedState(listener)

        DisposableEffect(key1 = Unit) {
            val observer = object : LifecycleEventObserver {
                override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                    when (event) {
                        Lifecycle.Event.ON_RESUME -> {
                            if (navBackStackEntry.savedStateHandle.contains(resultKey)) {
                                currentListener(navBackStackEntry.savedStateHandle.remove<R>(resultKey) as R)
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
}