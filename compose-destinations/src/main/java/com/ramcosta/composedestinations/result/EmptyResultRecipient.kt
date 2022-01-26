package com.ramcosta.composedestinations.result

import androidx.compose.runtime.Composable
import com.ramcosta.composedestinations.spec.DestinationSpec

class EmptyResultRecipient<D : DestinationSpec<*>, R> : ResultRecipient<D, R> {

    @Composable
    override fun onResult(lambda: (R) -> Unit) = Unit
}