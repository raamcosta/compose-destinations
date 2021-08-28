package com.ramcosta.composedestinations.codegen.model

sealed class DefaultValue {
    object Unknown : DefaultValue()
    object None : DefaultValue()
    data class Known(val srcCode: String) : DefaultValue()
}