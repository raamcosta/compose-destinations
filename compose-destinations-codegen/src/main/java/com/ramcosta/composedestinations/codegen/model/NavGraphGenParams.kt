package com.ramcosta.composedestinations.codegen.model

import com.ramcosta.composedestinations.codegen.commons.toSnakeCase
import com.ramcosta.composedestinations.codegen.moduleName
import java.util.Locale

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
    override val externalNavGraphs: List<ExternalRoute.NavGraph>,
    override val externalDestinations: List<ExternalRoute.Destination>,
    override val externalStartRoute: ExternalRoute?,
    override val name: String,
    override val baseRoute: String,
    val baseRouteWithNoModulePrefix: String,
) : NavGraphGenParams {

    companion object {
        operator fun invoke(
            annotationType: Importable,
            isNavHostGraph: Boolean,
            defaultTransitions: Importable?,
            deepLinks: List<DeepLink>,
            navArgs: RawNavArgsClass?,
            sourceIds: List<String>,
            parent: Importable? = null,
            isParentStart: Boolean? = null,
            visibility: Visibility,
            externalRoutes: List<ExternalRoute>,
            routeOverride: String? = null
        ): RawNavGraphGenParams {
            val name: String = annotationType.simpleName.let {
                if (it.endsWith("NavGraph")) {
                    it.removeSuffix("NavGraph") + "Graph"
                } else if (it.endsWith("Graph")) {
                    it.removeSuffix("Graph") + "NavGraph"
                } else {
                    it + "NavGraph"
                }
            }

            fun String.prepareForRouteFormat() = this
                .replace("(?i)navgraph".toRegex(), "")
                .replace("(?i)graph".toRegex(), "")
                .toSnakeCase()

            fun nameWithModuleName(): String {
                val moduleNamePrefix = moduleName.takeIf { it.isNotBlank() }?.let { "${it.toSnakeCase()}/" } ?: ""
                return "$moduleNamePrefix${name.replaceFirstChar { it.lowercase(Locale.US) }}"
            }

            val externalNavGraphs = externalRoutes.filterIsInstance<ExternalRoute.NavGraph>()
            val externalDestinations = externalRoutes.filterIsInstance<ExternalRoute.Destination>()
            val externalStartRoute = externalRoutes.find { it.isStart }

            val baseRoute = routeOverride ?: nameWithModuleName().prepareForRouteFormat()

            val baseRouteWithNoModulePrefix = routeOverride ?: name.prepareForRouteFormat()

            return RawNavGraphGenParams(
                annotationType = annotationType,
                isNavHostGraph = isNavHostGraph,
                defaultTransitions = defaultTransitions,
                deepLinks = deepLinks,
                navArgs = navArgs,
                sourceIds = sourceIds,
                parent = parent,
                isParentStart = isParentStart,
                visibility = visibility,
                externalRoutes = externalRoutes,
                externalNavGraphs = externalNavGraphs,
                externalDestinations = externalDestinations,
                externalStartRoute = externalStartRoute,
                name = name,
                baseRoute = baseRoute,
                baseRouteWithNoModulePrefix = baseRouteWithNoModulePrefix
            )
        }
    }
}
