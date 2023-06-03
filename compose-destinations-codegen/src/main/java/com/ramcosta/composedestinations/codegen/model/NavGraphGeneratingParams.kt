package com.ramcosta.composedestinations.codegen.model

data class NavGraphGeneratingParams(
    val rawParams: RawNavGraphGenParams,
    val route: String,
    val destinations: List<GeneratedDestination>,
    val startRouteFieldName: String,
    val nestedNavGraphRoutes: List<String>,
    val requireOptInAnnotationTypes: Set<Importable>,
    val defaultTransitions: Importable?,
    val isNavHostGraph: Boolean
)