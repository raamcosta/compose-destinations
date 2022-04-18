package com.ramcosta.composedestinations.codegen.model

data class GeneratedDestination(
    val sourceIds: List<String>,
    val qualifiedName: String,
    val simpleName: String,
    val navGraphInfo: NavGraphInfo,
    val requireOptInAnnotationTypes: List<Importable>
)