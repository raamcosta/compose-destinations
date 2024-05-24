package com.ramcosta.composedestinations.codegen.model

data class DeepLink(
    val action: String,
    val mimeType: String,
    val uriPattern: String,
)