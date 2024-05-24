package com.ramcosta.composedestinations.codegen.model

data class DefaultValue(
    val code: String,
    val imports: List<String> = emptyList()
)