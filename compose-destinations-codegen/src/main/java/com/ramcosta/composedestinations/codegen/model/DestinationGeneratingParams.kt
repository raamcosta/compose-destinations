package com.ramcosta.composedestinations.codegen.model

import com.ramcosta.composedestinations.codegen.commons.GENERATED_DESTINATION_SUFFIX
import com.ramcosta.composedestinations.codegen.commons.toSnakeCase
import com.ramcosta.composedestinations.codegen.commons.toValidClassName
import com.ramcosta.composedestinations.codegen.moduleName

interface DestinationGeneratingParams {
    val sourceIds: List<String>
    val name: String
    val annotatedName: String
    val annotatedQualifiedName: String
    val visibility: Visibility
    val baseRoute: String
    val parameters: List<Parameter>
    val deepLinks: List<DeepLink>
    val navGraphInfo: NavGraphInfo?
    val destinationStyleType: DestinationStyleType
    val composableReceiverSimpleName: String?
    val requireOptInAnnotationTypes: List<Importable>
    val destinationNavArgsClass: RawNavArgsClass?
    val activityDestinationParams: ActivityDestinationParams?
    val composableWrappers: List<Importable>
    val isParentStart: Boolean
}

data class RawDestinationGenParams(
    override val sourceIds: List<String>,
    override val annotatedName: String,
    override val annotatedQualifiedName: String,
    override val visibility: Visibility,
    override val parameters: List<Parameter>,
    override val deepLinks: List<DeepLink>,
    override val navGraphInfo: NavGraphInfo?,
    override val destinationStyleType: DestinationStyleType,
    override val composableReceiverSimpleName: String?,
    override val requireOptInAnnotationTypes: List<Importable>,
    override val destinationNavArgsClass: RawNavArgsClass?,
    override val activityDestinationParams: ActivityDestinationParams? = null,
    override val composableWrappers: List<Importable>,
    override val isParentStart: Boolean,
    private val hasMultipleDestinations: Boolean,
    private val routeOverride: String?,
) : DestinationGeneratingParams {

    private val destinationNames: DestinationNames by lazy {
        if (routeOverride != null) {
            return@lazy DestinationNames(
                route = routeOverride,
                destination = routeOverride.toValidClassName() + GENERATED_DESTINATION_SUFFIX
            )
        }

        val moduleNamePrefix = moduleName.takeIf { it.isNotBlank() }?.let { "${it.toSnakeCase()}/" } ?: ""
        val routeWithNoModule = if (hasMultipleDestinations) {
            val navGraphName = navGraphInfo?.graphType?.simpleName
                ?.removeSuffix("NavGraph")
                ?.removeSuffix("Graph")
                .orEmpty()
            "${navGraphName.toSnakeCase()}/${annotatedName.toSnakeCase()}"
        } else {
            annotatedName.toSnakeCase()
        }

        DestinationNames(
            route = "$moduleNamePrefix$routeWithNoModule",
            destination = routeWithNoModule.toValidClassName() + GENERATED_DESTINATION_SUFFIX
        )
    }

    override val name: String get() = destinationNames.destination
    override val baseRoute: String get() = destinationNames.route

    private class DestinationNames(val route: String, val destination: String)
}