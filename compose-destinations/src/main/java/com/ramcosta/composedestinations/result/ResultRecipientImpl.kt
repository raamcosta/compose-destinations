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

class ResultRecipientImpl<D : DestinationSpec<*>, R>(
    private val navBackStackEntry: NavBackStackEntry,
    resultOriginType: Class<D>,
    resultType: Class<R>,
) : ResultRecipient<D, R> {

    private val resultKey = resultKey(resultOriginType, resultType)

    @Composable
    override fun onResult(lambda: (R) -> Unit) {
        println("DestinationResultRecipientImpl observe called")
        DisposableEffect(key1 = Unit) {
            println("SideEffect INSIDE")
            println("DestinationResultRecipientImpl -> resultName = $resultKey")

            navBackStackEntry.lifecycle.addObserver(object : LifecycleEventObserver {
                override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                    when (event) {
                        Lifecycle.Event.ON_RESUME -> {
                            if (navBackStackEntry.savedStateHandle.contains(resultKey)) {
                                lambda(navBackStackEntry.savedStateHandle.remove<R>(resultKey) as R)
                            }
                        }

                        Lifecycle.Event.ON_DESTROY -> {
                            navBackStackEntry.lifecycle.removeObserver(this)
                        }

                        else -> Unit
                    }
                }
            })

//            navBackStackEntry
//                .savedStateHandle
//                .getLiveData<R>(resultName, null)
//                .observe(navBackStackEntry) { result ->
//                    if (result != null) {
//                        navBackStackEntry.savedStateHandle.set<R>(resultName, null)
//                        navBackStackEntry.lifecycle.addObserver(object : LifecycleEventObserver {
//                            override fun onStateChanged(
//                                source: LifecycleOwner,
//                                event: Lifecycle.Event
//                            ) {
//                                if (event == Lifecycle.Event.ON_RESUME) {
//                                    navBackStackEntry.lifecycle.removeObserver(this)
//                                    lambda(result)
//                                }
//                            }
//                        })
//                    }
//                }

            onDispose {
                println("SideEffect onDispose")
            }
        }
    }
}