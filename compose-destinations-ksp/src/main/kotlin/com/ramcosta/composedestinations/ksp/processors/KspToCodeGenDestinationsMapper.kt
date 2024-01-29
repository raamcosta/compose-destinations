package com.ramcosta.composedestinations.ksp.processors

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Modifier
import com.ramcosta.composedestinations.codegen.commons.ACTIVITY_DESTINATION_ANNOTATION
import com.ramcosta.composedestinations.codegen.commons.ACTIVITY_DESTINATION_ANNOTATION_DEFAULT_NULL
import com.ramcosta.composedestinations.codegen.commons.CORE_PACKAGE_NAME
import com.ramcosta.composedestinations.codegen.commons.DESTINATION_ANNOTATION
import com.ramcosta.composedestinations.codegen.commons.DESTINATION_ANNOTATION_DEEP_LINKS_ARGUMENT
import com.ramcosta.composedestinations.codegen.commons.DESTINATION_ANNOTATION_DEFAULT_ROUTE_PLACEHOLDER
import com.ramcosta.composedestinations.codegen.commons.DESTINATION_ANNOTATION_ROUTE_ARGUMENT
import com.ramcosta.composedestinations.codegen.commons.GENERATED_DESTINATION_SUFFIX
import com.ramcosta.composedestinations.codegen.commons.IllegalDestinationsSetup
import com.ramcosta.composedestinations.codegen.commons.NAV_GRAPH_ANNOTATION
import com.ramcosta.composedestinations.codegen.commons.NAV_GRAPH_ANNOTATION_QUALIFIED
import com.ramcosta.composedestinations.codegen.commons.NAV_HOST_GRAPH_ANNOTATION
import com.ramcosta.composedestinations.codegen.commons.NAV_HOST_GRAPH_ANNOTATION_QUALIFIED
import com.ramcosta.composedestinations.codegen.commons.rootNavGraphType
import com.ramcosta.composedestinations.codegen.commons.toSnakeCase
import com.ramcosta.composedestinations.codegen.commons.toValidClassName
import com.ramcosta.composedestinations.codegen.model.ActivityDestinationParams
import com.ramcosta.composedestinations.codegen.model.DestinationStyleType
import com.ramcosta.composedestinations.codegen.model.Importable
import com.ramcosta.composedestinations.codegen.model.NavGraphInfo
import com.ramcosta.composedestinations.codegen.model.NavTypeSerializer
import com.ramcosta.composedestinations.codegen.model.RawDestinationGenParams
import com.ramcosta.composedestinations.codegen.model.Visibility
import com.ramcosta.composedestinations.ksp.commons.DestinationMappingUtils
import com.ramcosta.composedestinations.ksp.commons.MutableKSFileSourceMapper
import com.ramcosta.composedestinations.ksp.commons.NavArgsTypeWithFile
import com.ramcosta.composedestinations.ksp.commons.findAllRequireOptInAnnotations
import com.ramcosta.composedestinations.ksp.commons.findAnnotationPathRecursively
import com.ramcosta.composedestinations.ksp.commons.findArgumentValue
import com.ramcosta.composedestinations.ksp.commons.getNavArgsDelegateType
import com.ramcosta.composedestinations.ksp.commons.ignoreAnnotations
import com.ramcosta.composedestinations.ksp.commons.isNothing
import com.ramcosta.composedestinations.ksp.commons.toDeepLink
import com.ramcosta.composedestinations.ksp.commons.toGenVisibility
import com.ramcosta.composedestinations.ksp.commons.toParameter

internal class KspToCodeGenDestinationsMapper(
    private val resolver: Resolver,
    private val destinationMappingUtils: DestinationMappingUtils,
    private val sourceFileMapper: MutableKSFileSourceMapper,
    private val navTypeSerializersByType: Map<Importable, NavTypeSerializer>,
) {

    fun map(
        composableDestinations: Sequence<DestinationAnnotationsPath>,
        activityDestinations: Sequence<KSClassDeclaration> //TODO RACOSTA do we need to have a path as well for activity? 🤔
    ): List<RawDestinationGenParams> {
        return composableDestinations.map { it.toDestination() }.toList() +
                activityDestinations.map { it.toActivityDestination() }.toList()
    }

    private fun DestinationAnnotationsPath.toDestination(): RawDestinationGenParams {
        val composableName = function.simpleName.asString()

        val deepLinksAnnotations = annotations.findCumulativeArgumentValue {
            findArgumentValue<ArrayList<KSAnnotation>>(DESTINATION_ANNOTATION_DEEP_LINKS_ARGUMENT)
        }

        val cleanRoute = annotations.findOverridingArgumentValue { prepareRoute(composableName) }!!
        val isStart = annotations.findOverridingArgumentValue { findArgumentValue<Boolean>("start") }!!
        val name = cleanRoute.toValidClassName() + GENERATED_DESTINATION_SUFFIX

        val navArgsDelegateTypeAndFile = annotations.getNavArgsDelegateType()
        if (navArgsDelegateTypeAndFile?.file != null) {
            sourceFileMapper[navArgsDelegateTypeAndFile.file.filePath] = navArgsDelegateTypeAndFile.file
        }
        sourceFileMapper[function.containingFile!!.filePath] = function.containingFile

        return RawDestinationGenParams(
            sourceIds = listOfNotNull(function.containingFile!!.filePath, navArgsDelegateTypeAndFile?.file?.filePath),
            name = name,
            composableName = composableName,
            composableQualifiedName = function.qualifiedName!!.asString(),
            visibility = annotations.findOverridingArgumentValue { getDestinationVisibility() }!!,
            baseRoute = cleanRoute,
            destinationStyleType = annotations.findOverridingArgumentValue { destinationMappingUtils.getDestinationStyleType(this, "composable $composableName") }!!,
            parameters = function.parameters.map { it.toParameter(resolver, navTypeSerializersByType) },
            composableWrappers = annotations.findCumulativeArgumentValue { destinationMappingUtils.getDestinationWrappers(this) },
            deepLinks = deepLinksAnnotations.map { it.toDeepLink() },
            navGraphInfo = getNavGraphInfo().let { it.copy(start = it.start || isStart) },
            composableReceiverSimpleName = function.extensionReceiver?.toString(),
            requireOptInAnnotationTypes = function.findAllRequireOptInAnnotations(),
            destinationNavArgsClass = navArgsDelegateTypeAndFile?.type,
            isDetached = isDetachedRoute()
        )
    }

    private fun KSClassDeclaration.toActivityDestination(): RawDestinationGenParams {
        val activityDestinationAnnotations = findAnnotationPathRecursively(ACTIVITY_DESTINATION_ANNOTATION)!!.reversed()
        val deepLinksAnnotations = activityDestinationAnnotations.findCumulativeArgumentValue { findArgumentValue<ArrayList<KSAnnotation>>(DESTINATION_ANNOTATION_DEEP_LINKS_ARGUMENT) }
        val explicitActivityClass = activityDestinationAnnotations.findOverridingArgumentValue { findArgumentValue<KSType>("activityClass") }!!
            .declaration as KSClassDeclaration

        val isActivityClass = activityType.isAssignableFrom(this.asType(emptyList()))

        val finalActivityClass = getFinalActivityClass(isActivityClass, explicitActivityClass)

        val navArgsDelegateTypeAndFile =
            activityDestinationAnnotations.getNavArgsDelegateType()
        if (navArgsDelegateTypeAndFile?.file != null) {
            sourceFileMapper[navArgsDelegateTypeAndFile.file.filePath] = navArgsDelegateTypeAndFile.file
        }
        sourceFileMapper[containingFile!!.filePath] = containingFile

        return RawDestinationGenParams(
            sourceIds = listOf(containingFile!!.filePath),
            name = finalActivityClass.simpleName + GENERATED_DESTINATION_SUFFIX,
            composableName = finalActivityClass.simpleName,
            composableQualifiedName = finalActivityClass.qualifiedName,
            visibility = activityDestinationAnnotations.findOverridingArgumentValue { getDestinationVisibility() }!!,
            baseRoute = activityDestinationAnnotations.findOverridingArgumentValue { prepareRoute(finalActivityClass.simpleName) }!!,
            parameters = emptyList(),
            deepLinks = deepLinksAnnotations.map { it.toDeepLink() },
            navGraphInfo = getNavGraphInfo() ?: getDefaultNavGraphInfo(),
            destinationStyleType = DestinationStyleType.Activity,
            composableReceiverSimpleName = null,
            requireOptInAnnotationTypes = emptyList(),
            destinationNavArgsClass = navArgsDelegateTypeAndFile?.type,
            activityDestinationParams = ActivityDestinationParams(
                targetPackage = activityDestinationAnnotations.findOverridingArgumentValue { getNullableString("targetPackage") },
                action = activityDestinationAnnotations.findOverridingArgumentValue { getNullableString("action") },
                dataUri = activityDestinationAnnotations.findOverridingArgumentValue { getNullableString("dataUri") },
                dataPattern = activityDestinationAnnotations.findOverridingArgumentValue { getNullableString("dataPattern") }
            ),
            composableWrappers = emptyList(),
            isDetached = annotations.any { it.annotationType.resolve().declaration.isDetachedRouteAnnotation() }
        )
    }

    private fun KSAnnotation.getNullableString(name: String): String? {
        return findArgumentValue<String>(name).takeIf {
            it != ACTIVITY_DESTINATION_ANNOTATION_DEFAULT_NULL
        }
    }

    private fun KSClassDeclaration.getFinalActivityClass(
        isActivityClass: Boolean,
        explicitActivityClass: KSClassDeclaration
    ) = if (isActivityClass) {
        if (!explicitActivityClass.isNothing) {
            throw IllegalDestinationsSetup("When annotating Activity classes with \"@$ACTIVITY_DESTINATION_ANNOTATION\", you must not specify an \"activityClass\" in the annotation! (check ${this.simpleName.asString()})")
        }

        Importable(
            simpleName.asString(),
            qualifiedName!!.asString()
        )
    } else {
        if (explicitActivityClass.isNothing) {
            throw IllegalDestinationsSetup("When annotating non-Activity classes with \"@$ACTIVITY_DESTINATION_ANNOTATION\", you need to specify an \"activityClass\" in the annotation!")
        }

        Importable(
            explicitActivityClass.simpleName.asString(),
            explicitActivityClass.qualifiedName!!.asString()
        )
    }

    private fun List<KSAnnotation>.getNavArgsDelegateType(): NavArgsTypeWithFile? {
        var lastFound: NavArgsTypeWithFile? = null

        forEach { each ->
            each.getNavArgsDelegateType(resolver, navTypeSerializersByType)?.let {
                lastFound = it
            }
        }

        return lastFound
    }

    private inline fun <reified T: Any> List<KSAnnotation>.findOverridingArgumentValue(findArg: KSAnnotation.() -> T?): T? {
        var lastFound: T? = null

        forEach {
            findArg(it)?.let { foundArg ->
                lastFound = foundArg
            }
        }

        return lastFound
    }


    private inline fun <reified T> List<KSAnnotation>.findCumulativeArgumentValue(findArg: KSAnnotation.() -> List<T>?): List<T> {
        var cumulative: ArrayList<T>? = null

        forEach {
            findArg(it)?.let {
                cumulative = (cumulative ?: ArrayList()).apply { addAll(it) }
            }
        }

        return cumulative!!
    }

    private fun Sequence<KSAnnotation>.findNavGraphAnnotation(): NavGraphInfo? {
        var resolvedAnnotation: KSType? = null
        var isNavHostGraph = false

        val directNavGraphAnnotation = find {
            val annotationType = it.annotationType.resolve()
            if (annotationType.isNavGraphAnnotation()) {
                resolvedAnnotation = annotationType
                return@find true
            }

            if (annotationType.isNavHostGraphAnnotation()) {
                resolvedAnnotation = annotationType
                isNavHostGraph = true
                return@find true
            }

            return@find false
        }

        return directNavGraphAnnotation?.let {
            NavGraphInfo(
                start = directNavGraphAnnotation.arguments.first().value as Boolean,
                isNavHostGraph = isNavHostGraph,
                graphType = Importable(
                    resolvedAnnotation!!.declaration.simpleName.asString(),
                    resolvedAnnotation!!.declaration.qualifiedName!!.asString()
                )
            )
        }
    }

    private fun DestinationAnnotationsPath.getNavGraphInfo() =
        function.annotations.findNavGraphAnnotation()
            ?: annotationsOfAnnotationsResolved
                .asSequence().findNavGraphAnnotation() ?: getDefaultNavGraphInfo()

    private fun DestinationAnnotationsPath.isDetachedRoute(): Boolean {
        val detachedRoute = (functionAnnotationsResolved.find { it.declaration.isDetachedRouteAnnotation() }
            ?: annotationsOfAnnotationsResolved.find { it.shortName.asString() == "DetachedRoute" && it.annotationType.resolve().declaration.isDetachedRouteAnnotation() })
        return detachedRoute != null
    }

    private fun KSDeclaration.isDetachedRouteAnnotation(): Boolean {
        return qualifiedName?.asString() == "${CORE_PACKAGE_NAME}.annotation.DetachedRoute"
    }
    private fun KSDeclaration.getNavGraphInfo(): NavGraphInfo? {
        if (modifiers.contains(Modifier.ANNOTATION) &&
            annotations.any { it.annotationType.resolve().declaration.qualifiedName?.asString() == qualifiedName?.asString() }) {
            // If we're checking an annotation which is annotated with itself (like @Target)
            // then this won't contain any nav graph info - avoids stackoverflow
            return null
        }

        val relevantAnnotations = annotations.filter { functionAnnotation ->
            val annotationShortName = functionAnnotation.shortName.asString()
            annotationShortName !in (ignoreAnnotations + DESTINATION_ANNOTATION + ACTIVITY_DESTINATION_ANNOTATION)
        }

        var resolvedAnnotation: KSType? = null
        var isNavHostGraph = false
        val navGraphAnnotation = relevantAnnotations.find { functionAnnotation ->
            val functionAnnotationType = functionAnnotation.annotationType.resolve()

            val didWeFindNavGraph = functionAnnotationType.isNavGraphAnnotation()

            if (didWeFindNavGraph) {
                resolvedAnnotation = functionAnnotationType
                return@find true
            }

            val didWeFindNavHostGraph = functionAnnotationType.isNavHostGraphAnnotation()

            if (didWeFindNavHostGraph) {
                isNavHostGraph = true
                resolvedAnnotation = functionAnnotationType
                return@find true
            }

            false
        }

        return if (navGraphAnnotation == null) {
            relevantAnnotations.mapNotNull {
                it.annotationType.resolve().declaration.getNavGraphInfo()
            }.firstOrNull()
        } else {
            NavGraphInfo(
                start = navGraphAnnotation.arguments.first().value as Boolean,
                isNavHostGraph = isNavHostGraph,
                graphType = Importable(
                    resolvedAnnotation!!.declaration.simpleName.asString(),
                    resolvedAnnotation!!.declaration.qualifiedName!!.asString()
                )
            )
        }
    }

    private fun KSType.isNavGraphAnnotation(): Boolean {
        return declaration.annotations.any { annotationOfAnnotation ->
            annotationOfAnnotation.shortName.asString() == NAV_GRAPH_ANNOTATION
                    && annotationOfAnnotation.annotationType.resolve().declaration.qualifiedName?.asString() == NAV_GRAPH_ANNOTATION_QUALIFIED
        }
    }

    private fun KSType.isNavHostGraphAnnotation(): Boolean {
        return declaration.annotations.any { annotationOfAnnotation ->
            annotationOfAnnotation.shortName.asString() == NAV_HOST_GRAPH_ANNOTATION
                    && annotationOfAnnotation.annotationType.resolve().declaration.qualifiedName?.asString() == NAV_HOST_GRAPH_ANNOTATION_QUALIFIED
        }
    }

    private fun getDefaultNavGraphInfo(): NavGraphInfo {
        return NavGraphInfo(
            start = false,
            isNavHostGraph = true,
            graphType = rootNavGraphType
        )
    }

    private fun KSAnnotation.getDestinationVisibility(): Visibility? {
        return findArgumentValue<KSType>("visibility")?.toGenVisibility()
    }

    private fun KSAnnotation.prepareRoute(composableName: String): String? {
        val cleanRoute = findArgumentValue<String>(DESTINATION_ANNOTATION_ROUTE_ARGUMENT)
        return if (cleanRoute == DESTINATION_ANNOTATION_DEFAULT_ROUTE_PLACEHOLDER) composableName.toSnakeCase() else cleanRoute
    }

    private val activityType by lazy {
        resolver.getClassDeclarationByName("android.app.Activity")!!.asType(emptyList())
    }
}
