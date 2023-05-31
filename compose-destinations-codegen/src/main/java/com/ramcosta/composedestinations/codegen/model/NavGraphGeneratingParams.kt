package com.ramcosta.composedestinations.codegen.model

interface NavGraphGeneratingParams {
    val route: String
    val defaultTransitions: Importable?
    val isNavHostGraph: Boolean
    val destinations: List<GeneratedDestination>
    val startRouteFieldName: String
    val nestedNavGraphRoutes: List<String>
    val requireOptInAnnotationTypes: Set<Importable>
}

data class NavGraphGeneratingParamsImpl(
    val rawParams: RawNavGraphGenParams,
    override val route: String,
    override val destinations: List<GeneratedDestination>,
    override val startRouteFieldName: String,
    override val nestedNavGraphRoutes: List<String>,
    override val requireOptInAnnotationTypes: Set<Importable>,
    override val defaultTransitions: Importable?,
    override val isNavHostGraph: Boolean
): NavGraphGeneratingParams

data class LegacyNavGraphGeneratingParams(
    override val route: String,
    override val destinations: List<GeneratedDestination>,
    override val startRouteFieldName: String,
    override val nestedNavGraphRoutes: List<String>,
    override val requireOptInAnnotationTypes: Set<Importable>,
): NavGraphGeneratingParams {
    override val defaultTransitions: Importable? = null
    override val isNavHostGraph: Boolean = route == "root"
}