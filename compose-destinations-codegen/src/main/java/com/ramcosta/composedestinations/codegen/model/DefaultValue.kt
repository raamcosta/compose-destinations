package com.ramcosta.composedestinations.codegen.model

sealed interface DefaultValue

object Unknown : DefaultValue
object None : DefaultValue
data class Known(val srcCode: String) : DefaultValue