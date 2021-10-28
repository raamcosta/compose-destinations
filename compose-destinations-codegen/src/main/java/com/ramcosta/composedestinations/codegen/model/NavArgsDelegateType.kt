package com.ramcosta.composedestinations.codegen.model

data class NavArgsDelegateType(
    val navArgs: List<Parameter>,
    val qualifiedName: String,
    val simpleName: String
)