package com.ramcosta.composedestinations.codegen.model

data class Destination(
    val name: String,
    val qualifiedName: String,
    val composableName: String,
    val composableQualifiedName: String,
    val cleanRoute: String,
    val parameters: List<Parameter>,
    val deepLinks: List<DeepLink>,
    val isStart: Boolean,
    val navGraphRoute: String
)