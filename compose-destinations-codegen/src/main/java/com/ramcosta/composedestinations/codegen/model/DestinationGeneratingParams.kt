package com.ramcosta.composedestinations.codegen.model

interface DestinationGeneratingParams {
    val sourceIds: List<String>
    val name: String
    val composableName: String
    val composableQualifiedName: String
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
    override val name: String,
    override val composableName: String,
    override val composableQualifiedName: String,
    override val visibility: Visibility,
    override val baseRoute: String,
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
): DestinationGeneratingParams