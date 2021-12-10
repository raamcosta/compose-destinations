package com.ramcosta.composedestinations.codegen.model

data class GeneratedDestination(
    val sourceIds: List<String>,
    val qualifiedName: String,
    val simpleName: String,
    val isStartDestination: Boolean,
    val navGraphRoute: String,
    val requireOptInAnnotationTypes: List<ClassType>
)