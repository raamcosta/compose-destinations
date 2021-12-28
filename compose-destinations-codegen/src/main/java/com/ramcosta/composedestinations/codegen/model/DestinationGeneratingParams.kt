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
    val isStart: Boolean
    val navGraphRoute: String
    val destinationStyleType: DestinationStyleType
    val composableReceiverSimpleName: String?
    val requireOptInAnnotationTypes: List<ClassType>
    val navArgsDelegateType: NavArgsDelegateType?

    companion object {
        operator fun invoke(
            sourceIds: List<String>,
            name: String,
            qualifiedName: String,
            composableName: String,
            composableQualifiedName: String,
            cleanRoute: String,
            parameters: List<Parameter>,
            deepLinks: List<DeepLink>,
            isStart: Boolean,
            navGraphRoute: String,
            destinationStyleType: DestinationStyleType,
            composableReceiverSimpleName: String?,
            requireOptInAnnotationTypes: List<ClassType>,
            navArgsDelegateType: NavArgsDelegateType?
        ): DestinationGeneratingParams {
            return DestinationGeneratingParamsImpl(
                sourceIds = sourceIds,
                name = name,
                qualifiedName = qualifiedName,
                composableName = composableName,
                composableQualifiedName = composableQualifiedName,
                cleanRoute = cleanRoute,
                parameters = parameters,
                deepLinks = deepLinks,
                isStart = isStart,
                navGraphRoute = navGraphRoute,
                destinationStyleType = destinationStyleType,
                composableReceiverSimpleName = composableReceiverSimpleName,
                requireOptInAnnotationTypes = requireOptInAnnotationTypes,
                navArgsDelegateType = navArgsDelegateType
            )
        }
    }

}

data class DestinationGeneratingParamsImpl(
    override val sourceIds: List<String>,
    override val name: String,
    override val qualifiedName: String,
    override val composableName: String,
    override val composableQualifiedName: String,
    override val cleanRoute: String,
    override val parameters: List<Parameter>,
    override val deepLinks: List<DeepLink>,
    override val isStart: Boolean,
    override val navGraphRoute: String,
    override val destinationStyleType: DestinationStyleType,
    override val composableReceiverSimpleName: String?,
    override val requireOptInAnnotationTypes: List<ClassType>,
    override val navArgsDelegateType: NavArgsDelegateType?
): DestinationGeneratingParams