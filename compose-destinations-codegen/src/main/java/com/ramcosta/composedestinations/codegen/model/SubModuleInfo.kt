package com.ramcosta.composedestinations.codegen.model

data class SubModuleInfo(
    val name: String?,
    val genPackageName: String,
    val publicResultSenders: List<DestinationResultSenderInfo>
)

data class DestinationResultSenderInfo(
    val genDestinationQualifiedName: String,
    val resultTypeQualifiedName: String,
    val isResultTypeNullable: Boolean,
)