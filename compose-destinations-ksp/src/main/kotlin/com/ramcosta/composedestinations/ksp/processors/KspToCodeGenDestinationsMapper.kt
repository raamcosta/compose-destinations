package com.ramcosta.composedestinations.ksp.processors

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.isInternal
import com.google.devtools.ksp.isPrivate
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Modifier
import com.ramcosta.composedestinations.codegen.commons.ACTIVITY_DESTINATION_ANNOTATION
import com.ramcosta.composedestinations.codegen.commons.ACTIVITY_DESTINATION_ANNOTATION_DEFAULT_NULL
import com.ramcosta.composedestinations.codegen.commons.CORE_PACKAGE_NAME
import com.ramcosta.composedestinations.codegen.commons.DESTINATION_ANNOTATION
import com.ramcosta.composedestinations.codegen.commons.DESTINATION_ANNOTATION_DEEP_LINKS_ARGUMENT
import com.ramcosta.composedestinations.codegen.commons.DESTINATION_ANNOTATION_DEFAULT_ROUTE_PLACEHOLDER
import com.ramcosta.composedestinations.codegen.commons.DESTINATION_ANNOTATION_ROUTE_ARGUMENT
import com.ramcosta.composedestinations.codegen.commons.DESTINATION_ANNOTATION_STYLE_ARGUMENT
import com.ramcosta.composedestinations.codegen.commons.DESTINATION_ANNOTATION_WRAPPERS_ARGUMENT
import com.ramcosta.composedestinations.codegen.commons.GENERATED_DESTINATION_SUFFIX
import com.ramcosta.composedestinations.codegen.commons.IllegalDestinationsSetup
import com.ramcosta.composedestinations.codegen.commons.NAV_GRAPH_ANNOTATION
import com.ramcosta.composedestinations.codegen.commons.NAV_GRAPH_ANNOTATION_QUALIFIED
import com.ramcosta.composedestinations.codegen.commons.NAV_HOST_GRAPH_ANNOTATION
import com.ramcosta.composedestinations.codegen.commons.NAV_HOST_GRAPH_ANNOTATION_QUALIFIED
import com.ramcosta.composedestinations.codegen.commons.rootNavGraphType
import com.ramcosta.composedestinations.codegen.commons.toSnakeCase
import com.ramcosta.composedestinations.codegen.model.ActivityDestinationParams
import com.ramcosta.composedestinations.codegen.model.DestinationStyleType
import com.ramcosta.composedestinations.codegen.model.Importable
import com.ramcosta.composedestinations.codegen.model.NavGraphInfo
import com.ramcosta.composedestinations.codegen.model.NavTypeSerializer
import com.ramcosta.composedestinations.codegen.model.RawDestinationGenParams
import com.ramcosta.composedestinations.codegen.model.Visibility
import com.ramcosta.composedestinations.ksp.commons.MutableKSFileSourceMapper
import com.ramcosta.composedestinations.ksp.commons.NavArgsTypeWithFile
import com.ramcosta.composedestinations.ksp.commons.findActualClassDeclaration
import com.ramcosta.composedestinations.ksp.commons.findAllRequireOptInAnnotations
import com.ramcosta.composedestinations.ksp.commons.findAnnotationPathRecursively
import com.ramcosta.composedestinations.ksp.commons.findArgumentValue
import com.ramcosta.composedestinations.ksp.commons.getNavArgsDelegateType
import com.ramcosta.composedestinations.ksp.commons.ignoreAnnotations
import com.ramcosta.composedestinations.ksp.commons.isNothing
import com.ramcosta.composedestinations.ksp.commons.toDeepLink
import com.ramcosta.composedestinations.ksp.commons.toImportable
import com.ramcosta.composedestinations.ksp.commons.toParameter

internal class KspToCodeGenDestinationsMapper(
    private val resolver: Resolver,
    private val sourceFileMapper: MutableKSFileSourceMapper,
    private val navTypeSerializersByType: Map<Importable, NavTypeSerializer>,
) {

    fun map(
        composableDestinations: Sequence<KSFunctionDeclaration>,
        activityDestinations: Sequence<KSClassDeclaration>
    ): List<RawDestinationGenParams> {
        return composableDestinations.map { it.toDestination() }.toList() +
                activityDestinations.map { it.toActivityDestination() }.toList()
    }

    private fun KSFunctionDeclaration.toDestination(): RawDestinationGenParams {
        val composableName = simpleName.asString()
        val name = composableName + GENERATED_DESTINATION_SUFFIX
        val destinationAnnotations = findAnnotationPathRecursively(DESTINATION_ANNOTATION)!!.reversed()

        val deepLinksAnnotations = destinationAnnotations.findCumulativeArgumentValue { findArgumentValue<ArrayList<KSAnnotation>>(DESTINATION_ANNOTATION_DEEP_LINKS_ARGUMENT) }

        val cleanRoute = destinationAnnotations.findOverridingArgumentValue { prepareRoute(composableName) }!!

        val navArgsDelegateTypeAndFile = destinationAnnotations.getNavArgsDelegateType()
        if (navArgsDelegateTypeAndFile?.file != null) {
            sourceFileMapper[navArgsDelegateTypeAndFile.file.filePath] = navArgsDelegateTypeAndFile.file
        }
        sourceFileMapper[containingFile!!.filePath] = containingFile

        return RawDestinationGenParams(
            sourceIds = listOfNotNull(containingFile!!.filePath, navArgsDelegateTypeAndFile?.file?.filePath),
            name = name,
            composableName = composableName,
            composableQualifiedName = qualifiedName!!.asString(),
            visibility = getDestinationVisibility(),
            baseRoute = cleanRoute,
            destinationStyleType = destinationAnnotations.findOverridingArgumentValue { getDestinationStyleType(composableName) }!!,
            parameters = parameters.map { it.toParameter(resolver, navTypeSerializersByType) },
            composableWrappers = destinationAnnotations.findCumulativeArgumentValue { getDestinationWrappers() },
            deepLinks = deepLinksAnnotations.map { it.toDeepLink() },
            navGraphInfo = getNavGraphInfo() ?: getDefaultNavGraphInfo(),
            composableReceiverSimpleName = extensionReceiver?.toString(),
            requireOptInAnnotationTypes = findAllRequireOptInAnnotations(),
            destinationNavArgsClass = navArgsDelegateTypeAndFile?.type
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
            visibility = getDestinationVisibility(),
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
            composableWrappers = emptyList()
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

        forEach {
            it.getNavArgsDelegateType(resolver, navTypeSerializersByType)?.let {
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

    private fun KSDeclaration.getDestinationVisibility(): Visibility {
        if (isPrivate()) {
            throw IllegalDestinationsSetup("Composable functions annotated with @Destination cannot be private!")
        }

        return if (isInternal()) Visibility.INTERNAL else Visibility.PUBLIC
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

            val didWeFindNavGraph = functionAnnotationType.declaration.annotations.any { annotationOfAnnotation ->
                annotationOfAnnotation.shortName.asString() == NAV_GRAPH_ANNOTATION
                        && annotationOfAnnotation.annotationType.resolve().declaration.qualifiedName?.asString() == NAV_GRAPH_ANNOTATION_QUALIFIED
            }

            if (didWeFindNavGraph) {
                resolvedAnnotation = functionAnnotationType
                return@find true
            }

            val didWeFindNavHostGraph = functionAnnotationType.declaration.annotations.any { annotationOfAnnotation ->
                annotationOfAnnotation.shortName.asString() == NAV_HOST_GRAPH_ANNOTATION
                        && annotationOfAnnotation.annotationType.resolve().declaration.qualifiedName?.asString() == NAV_HOST_GRAPH_ANNOTATION_QUALIFIED
            }

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

    private fun getDefaultNavGraphInfo(): NavGraphInfo {
        return NavGraphInfo(
            start = false,
            isNavHostGraph = true,
            graphType = rootNavGraphType
        )
    }

    private fun KSAnnotation.getDestinationStyleType(composableName: String): DestinationStyleType? {
        val ksStyleType = findArgumentValue<KSType>(DESTINATION_ANNOTATION_STYLE_ARGUMENT)
            ?: return null

        if (defaultStyle.isAssignableFrom(ksStyleType)) {
            return DestinationStyleType.Default
        }

        if (bottomSheetStyle != null && bottomSheetStyle!!.isAssignableFrom(ksStyleType)) {
            return DestinationStyleType.BottomSheet
        }

        if (runtimeStyle.isAssignableFrom(ksStyleType)) {
            return DestinationStyleType.Runtime
        }

        val importable = ksStyleType.findActualClassDeclaration()?.toImportable() ?: throw IllegalDestinationsSetup("Parameter $DESTINATION_ANNOTATION_STYLE_ARGUMENT of Destination annotation in composable $composableName was not resolvable: please review it.")

        if (dialogStyle.isAssignableFrom(ksStyleType)) {
            return DestinationStyleType.Dialog(importable)
        }

        if (animatedStyle != null && animatedStyle!!.isAssignableFrom(ksStyleType)) {
            return DestinationStyleType.Animated(importable, ksStyleType.declaration.findAllRequireOptInAnnotations())
        }

        throw IllegalDestinationsSetup("Unknown style used on $composableName. Please recheck it.")
    }

    private fun KSAnnotation.getDestinationWrappers(): List<Importable>? {
        val ksTypes = findArgumentValue<ArrayList<KSType>>(DESTINATION_ANNOTATION_WRAPPERS_ARGUMENT)
            ?: return null

        return ksTypes.map {
            if ((it.declaration as? KSClassDeclaration)?.classKind != ClassKind.OBJECT) {
                throw IllegalDestinationsSetup("DestinationWrappers need to be objects! (check ${it.declaration.simpleName.asString()})")
            }

            Importable(
                it.declaration.simpleName.asString(),
                it.declaration.qualifiedName!!.asString()
            )
        }
    }

    private fun KSAnnotation.prepareRoute(composableName: String): String? {
        val cleanRoute = findArgumentValue<String>(DESTINATION_ANNOTATION_ROUTE_ARGUMENT)
        return if (cleanRoute == DESTINATION_ANNOTATION_DEFAULT_ROUTE_PLACEHOLDER) composableName.toSnakeCase() else cleanRoute
    }


    private val defaultStyle by lazy {
        resolver.getClassDeclarationByName("$CORE_PACKAGE_NAME.spec.DestinationStyle.Default")!!
            .asType(emptyList())
    }

    private val bottomSheetStyle by lazy {
        resolver.getClassDeclarationByName("$CORE_PACKAGE_NAME.bottomsheet.spec.DestinationStyleBottomSheet")?.asType(emptyList())
    }

    private val animatedStyle by lazy {
        resolver.getClassDeclarationByName("$CORE_PACKAGE_NAME.spec.DestinationStyle.Animated")?.asType(emptyList())
    }

    private val dialogStyle by lazy {
        resolver.getClassDeclarationByName("$CORE_PACKAGE_NAME.spec.DestinationStyle.Dialog")!!.asType(emptyList())
    }

    private val runtimeStyle by lazy {
        resolver.getClassDeclarationByName("$CORE_PACKAGE_NAME.spec.DestinationStyle.Runtime")!!.asType(emptyList())
    }

    private val activityType by lazy {
        resolver.getClassDeclarationByName("android.app.Activity")!!.asType(emptyList())
    }
}
