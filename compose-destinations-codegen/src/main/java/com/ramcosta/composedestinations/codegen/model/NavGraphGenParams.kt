package com.ramcosta.composedestinations.codegen.model

import com.ramcosta.composedestinations.codegen.commons.toSnakeCase

interface NavGraphGenParams {
    val sourceIds: List<String>
    val name: String
    val baseRoute: String
    val annotationType: Importable
    val isNavHostGraph: Boolean
    val defaultTransitions: Importable?
    val deepLinks: List<DeepLink>
    val navArgs: RawNavArgsClass?
    val parent: Importable?
    val isParentStart: Boolean?
    val visibility: Visibility
    val externalRoutes: List<ExternalRoute>
    val externalNavGraphs: List<ExternalRoute.NavGraph>
    val externalDestinations: List<ExternalRoute.Destination>
    val externalStartRoute: ExternalRoute?
}

data class RawNavGraphGenParams(
    override val annotationType: Importable,
    override val isNavHostGraph: Boolean,
    override val defaultTransitions: Importable?,
    override val deepLinks: List<DeepLink>,
    override val navArgs: RawNavArgsClass?,
    override val sourceIds: List<String>,
    override val parent: Importable? = null,
    override val isParentStart: Boolean? = null,
    override val visibility: Visibility,
    override val externalRoutes: List<ExternalRoute>,
    private val routeOverride: String? = null,
) : NavGraphGenParams {

    override val externalNavGraphs: List<ExternalRoute.NavGraph> = externalRoutes.filterIsInstance<ExternalRoute.NavGraph>()
    override val externalDestinations: List<ExternalRoute.Destination> = externalRoutes.filterIsInstance<ExternalRoute.Destination>()
    override val externalStartRoute = externalRoutes.find { it.isStart }

    override val name: String = annotationType.simpleName.let {
        if (it.endsWith("NavGraph")) {
            it.removeSuffix("NavGraph") + "Graph"
        } else if (it.endsWith("Graph")) {
            it.removeSuffix("Graph") + "NavGraph"
        } else {
            it + "NavGraph"
        }
    }

    override val baseRoute: String by lazy(LazyThreadSafetyMode.NONE) {
        routeOverride ?: name.replace("(?i)graph".toRegex(), "").toSnakeCase()
    }
}
