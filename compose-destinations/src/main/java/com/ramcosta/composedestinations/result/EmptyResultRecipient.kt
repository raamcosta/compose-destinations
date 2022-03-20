@file:SuppressLint("ComposableNaming")
package com.ramcosta.composedestinations.result

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import com.ramcosta.composedestinations.spec.DestinationSpec

/**
 * Empty implementation of [ResultRecipient] to
 * use in previews and possibly testing.
 */
class EmptyResultRecipient<D : DestinationSpec<*>, R> : ResultRecipient<D, R> {

    @Suppress("OverridingDeprecatedMember")
    @Composable
    override fun onResult(listener: (R) -> Unit) = Unit

    @Composable
    override fun onNavResult(listener: (NavResult<R>) -> Unit) = Unit

}