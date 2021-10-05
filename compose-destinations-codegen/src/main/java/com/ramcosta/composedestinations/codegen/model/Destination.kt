package com.ramcosta.composedestinations.codegen.model

data class Destination(
    val sourceIds: List<String>,
    val name: String,
    val qualifiedName: String,
    val composableName: String,
    val composableQualifiedName: String,
    val cleanRoute: String,
    val parameters: List<Parameter>,
    val deepLinks: List<DeepLink>,
    val isStart: Boolean,
    val navGraphRoute: String,
    val transitionsSpecType: Type?,
    val composableReceiverSimpleName: String?,
    val requireOptInAnnotationNames: List<String>
)