package com.ramcosta.composedestinations.codegen.model

interface DestinationGeneratingParams {
    val sourceIds: List<String>
    val name: String
    val qualifiedName: String
    val composableName: String
    val composableQualifiedName: String
    val cleanRoute: String
    val parameters: List<Parameter>
    val deepLinks: List<DeepLink>
    val navGraphInfo: NavGraphInfo
    val destinationStyleType: DestinationStyleType
    val composableReceiverSimpleName: String?
    val requireOptInAnnotationTypes: List<ClassType>
    val navArgsDelegateType: NavArgsDelegateType?
}

data class RawDestinationGenParams(
    override val sourceIds: List<String>,
    override val name: String,
    override val qualifiedName: String,
    override val composableName: String,
    override val composableQualifiedName: String,
    override val cleanRoute: String,
    override val parameters: List<Parameter>,
    override val deepLinks: List<DeepLink>,
    override val navGraphInfo: NavGraphInfo,
    override val destinationStyleType: DestinationStyleType,
    override val composableReceiverSimpleName: String?,
    override val requireOptInAnnotationTypes: List<ClassType>,
    override val navArgsDelegateType: NavArgsDelegateType?
): DestinationGeneratingParams