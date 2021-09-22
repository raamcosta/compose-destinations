package com.ramcosta.composedestinations.codegen.model

data class GeneratedDestination(
    val sourceId: String,
    val qualifiedName: String,
    val simpleName: String,
    val isStartDestination: Boolean,
    val navGraphRoute: String
)