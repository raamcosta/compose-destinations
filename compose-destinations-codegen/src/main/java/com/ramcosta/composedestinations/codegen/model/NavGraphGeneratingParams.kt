package com.ramcosta.composedestinations.codegen.model

data class NavGraphGeneratingParams(
    val route: String,
    val destinations: List<GeneratedDestination>,
    val nestedNavGraphs: List<String>,
    val requireOptInAnnotationTypes: Set<ClassType>
)