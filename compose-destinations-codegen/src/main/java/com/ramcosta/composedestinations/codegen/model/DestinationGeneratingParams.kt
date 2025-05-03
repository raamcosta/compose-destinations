package com.ramcosta.composedestinations.codegen.model

import com.ramcosta.composedestinations.codegen.commons.GENERATED_DESTINATION_SUFFIX
import com.ramcosta.composedestinations.codegen.commons.toSnakeCase
import com.ramcosta.composedestinations.codegen.commons.toValidClassName
import com.ramcosta.composedestinations.codegen.moduleName

interface DestinationGeneratingParams {
    val label: String?
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
    val composableReceiverType: TypeInfo?
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
    override val composableReceiverType: TypeInfo?,
    override val requireOptInAnnotationTypes: List<Importable>,
    override val destinationNavArgsClass: RawNavArgsClass?,
    override val activityDestinationParams: ActivityDestinationParams? = null,
    override val composableWrappers: List<Importable>,
    override val isParentStart: Boolean,
    override val name: String,
    override val baseRoute: String,
    override val label: String?
) : DestinationGeneratingParams {

    companion object {
        operator fun invoke(
            sourceIds: List<String>,
            annotatedName: String,
            annotatedQualifiedName: String,
            visibility: Visibility,
            parameters: List<Parameter>,
            deepLinks: List<DeepLink>,
            navGraphInfo: NavGraphInfo?,
            destinationStyleType: DestinationStyleType,
            composableReceiverType: TypeInfo?,
            requireOptInAnnotationTypes: List<Importable>,
            destinationNavArgsClass: RawNavArgsClass?,
            activityDestinationParams: ActivityDestinationParams? = null,
            composableWrappers: List<Importable>,
            isParentStart: Boolean,
            hasMultipleDestinations: Boolean,
            routeOverride: String?,
            label: String?
        ): RawDestinationGenParams {
            class DestinationNames(val route: String, val destination: String)
            val names = if (routeOverride != null) {
                DestinationNames(
                    route = routeOverride,
                    destination = routeOverride.toValidClassName() + GENERATED_DESTINATION_SUFFIX
                )
            } else {
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

            return RawDestinationGenParams(
                sourceIds = sourceIds,
                annotatedName = annotatedName,
                annotatedQualifiedName = annotatedQualifiedName,
                visibility = visibility,
                parameters = parameters,
                deepLinks = deepLinks,
                navGraphInfo = navGraphInfo,
                destinationStyleType = destinationStyleType,
                composableReceiverType = composableReceiverType,
                requireOptInAnnotationTypes = requireOptInAnnotationTypes,
                destinationNavArgsClass = destinationNavArgsClass,
                activityDestinationParams = activityDestinationParams,
                composableWrappers = composableWrappers,
                isParentStart = isParentStart,
                name = names.destination,
                baseRoute = names.route,
                label = label,
            )
        }
    }
}