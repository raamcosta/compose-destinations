package com.ramcosta.composedestinations.ksp.processors

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.isInternal
import com.google.devtools.ksp.isPrivate
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.FileLocation
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeAlias
import com.google.devtools.ksp.symbol.KSTypeParameter
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.Location
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.symbol.Variance
import com.ramcosta.composedestinations.codegen.commons.ACTIVITY_DESTINATION_ANNOTATION
import com.ramcosta.composedestinations.codegen.commons.ACTIVITY_DESTINATION_ANNOTATION_DEFAULT_NULL
import com.ramcosta.composedestinations.codegen.commons.CORE_PACKAGE_NAME
import com.ramcosta.composedestinations.codegen.commons.DESTINATION_ANNOTATION
import com.ramcosta.composedestinations.codegen.commons.DESTINATION_ANNOTATION_DEEP_LINKS_ARGUMENT
import com.ramcosta.composedestinations.codegen.commons.DESTINATION_ANNOTATION_DEFAULT_ROUTE_PLACEHOLDER
import com.ramcosta.composedestinations.codegen.commons.DESTINATION_ANNOTATION_NAV_ARGS_DELEGATE_ARGUMENT
import com.ramcosta.composedestinations.codegen.commons.DESTINATION_ANNOTATION_NAV_GRAPH_ARGUMENT
import com.ramcosta.composedestinations.codegen.commons.DESTINATION_ANNOTATION_ROUTE_ARGUMENT
import com.ramcosta.composedestinations.codegen.commons.DESTINATION_ANNOTATION_START_ARGUMENT
import com.ramcosta.composedestinations.codegen.commons.DESTINATION_ANNOTATION_STYLE_ARGUMENT
import com.ramcosta.composedestinations.codegen.commons.DESTINATION_ANNOTATION_WRAPPERS_ARGUMENT
import com.ramcosta.composedestinations.codegen.commons.GENERATED_DESTINATION_SUFFIX
import com.ramcosta.composedestinations.codegen.commons.IllegalDestinationsSetup
import com.ramcosta.composedestinations.codegen.commons.NAV_GRAPH_ANNOTATION_QUALIFIED
import com.ramcosta.composedestinations.codegen.commons.NAV_HOST_PARAM_ANNOTATION_QUALIFIED
import com.ramcosta.composedestinations.codegen.commons.rootNavGraphType
import com.ramcosta.composedestinations.codegen.commons.toSnakeCase
import com.ramcosta.composedestinations.codegen.model.ActivityDestinationParams
import com.ramcosta.composedestinations.codegen.model.DeepLink
import com.ramcosta.composedestinations.codegen.model.DestinationStyleType
import com.ramcosta.composedestinations.codegen.model.Importable
import com.ramcosta.composedestinations.codegen.model.NavArgsDelegateType
import com.ramcosta.composedestinations.codegen.model.NavGraphInfo
import com.ramcosta.composedestinations.codegen.model.NavTypeSerializer
import com.ramcosta.composedestinations.codegen.model.Parameter
import com.ramcosta.composedestinations.codegen.model.RawDestinationGenParams
import com.ramcosta.composedestinations.codegen.model.Type
import com.ramcosta.composedestinations.codegen.model.TypeArgument
import com.ramcosta.composedestinations.codegen.model.TypeInfo
import com.ramcosta.composedestinations.codegen.model.ValueClassInnerInfo
import com.ramcosta.composedestinations.codegen.model.Visibility
import com.ramcosta.composedestinations.ksp.commons.KSFileSourceMapper
import com.ramcosta.composedestinations.ksp.commons.findActualClassDeclaration
import com.ramcosta.composedestinations.ksp.commons.findAllRequireOptInAnnotations
import com.ramcosta.composedestinations.ksp.commons.findAnnotationPathRecursively
import com.ramcosta.composedestinations.ksp.commons.findArgumentValue
import com.ramcosta.composedestinations.ksp.commons.getDefaultValue
import com.ramcosta.composedestinations.ksp.commons.ignoreAnnotations
import com.ramcosta.composedestinations.ksp.commons.isNothing
import com.ramcosta.composedestinations.ksp.commons.readLines
import com.ramcosta.composedestinations.ksp.commons.toImportable
import java.io.File

class KspToCodeGenDestinationsMapper(
    private val resolver: Resolver,
    private val navTypeSerializersByType: Map<Importable, NavTypeSerializer>,
) : KSFileSourceMapper {

    private val sourceFilesById = mutableMapOf<String, KSFile?>()

    fun map(
        composableDestinations: Sequence<KSFunctionDeclaration>,
        activityDestinations: Sequence<KSClassDeclaration>
    ): List<RawDestinationGenParams> {
        return composableDestinations.map { it.toDestination() }.toList() +
                activityDestinations.map { it.toActivityDestination() }.toList()
    }

    override fun mapToKSFile(sourceId: String): KSFile? {
        return sourceFilesById[sourceId]
    }

    private fun KSFunctionDeclaration.toDestination(): RawDestinationGenParams {
        val composableName = simpleName.asString()
        val name = composableName + GENERATED_DESTINATION_SUFFIX
        val destinationAnnotations = findAnnotationPathRecursively(DESTINATION_ANNOTATION)!!.reversed()

        val deepLinksAnnotations = destinationAnnotations.findCumulativeArgumentValue { findArgumentValue<ArrayList<KSAnnotation>>(DESTINATION_ANNOTATION_DEEP_LINKS_ARGUMENT) }

        val cleanRoute = destinationAnnotations.findOverridingArgumentValue { prepareRoute(composableName) }!!

        val navArgsDelegateTypeAndFile = destinationAnnotations.getNavArgsDelegateType(composableName)
        if (navArgsDelegateTypeAndFile?.file != null) {
            sourceFilesById[navArgsDelegateTypeAndFile.file.fileName] = navArgsDelegateTypeAndFile.file
        }
        sourceFilesById[containingFile!!.fileName] = containingFile

        return RawDestinationGenParams(
            sourceIds = listOfNotNull(containingFile!!.fileName, navArgsDelegateTypeAndFile?.file?.fileName),
            name = name,
            composableName = composableName,
            composableQualifiedName = qualifiedName!!.asString(),
            visibility = getDestinationVisibility(),
            cleanRoute = cleanRoute,
            destinationStyleType = destinationAnnotations.findOverridingArgumentValue { getDestinationStyleType(composableName) }!!,
            parameters = parameters.map { it.toParameter(composableName) },
            composableWrappers = destinationAnnotations.findCumulativeArgumentValue { getDestinationWrappers() },
            deepLinks = deepLinksAnnotations.map { it.toDeepLink() },
            navGraphInfo = getNavGraphInfo() ?: getDefaultNavGraphInfo(destinationAnnotations),
            composableReceiverSimpleName = extensionReceiver?.toString(),
            requireOptInAnnotationTypes = findAllRequireOptInAnnotations(),
            navArgsDelegateType = navArgsDelegateTypeAndFile?.type
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
            activityDestinationAnnotations.getNavArgsDelegateType(finalActivityClass.simpleName)
        if (navArgsDelegateTypeAndFile?.file != null) {
            sourceFilesById[navArgsDelegateTypeAndFile.file.fileName] = navArgsDelegateTypeAndFile.file
        }
        sourceFilesById[containingFile!!.fileName] = containingFile

        return RawDestinationGenParams(
            sourceIds = listOf(containingFile!!.fileName),
            name = finalActivityClass.simpleName + GENERATED_DESTINATION_SUFFIX,
            composableName = finalActivityClass.simpleName,
            composableQualifiedName = finalActivityClass.qualifiedName,
            visibility = getDestinationVisibility(),
            cleanRoute = activityDestinationAnnotations.findOverridingArgumentValue { prepareRoute(finalActivityClass.simpleName) }!!,
            parameters = emptyList(),
            deepLinks = deepLinksAnnotations.map { it.toDeepLink() },
            navGraphInfo = getNavGraphInfo() ?: getDefaultNavGraphInfo(activityDestinationAnnotations, true),
            destinationStyleType = DestinationStyleType.Activity,
            composableReceiverSimpleName = null,
            requireOptInAnnotationTypes = emptyList(),
            navArgsDelegateType = navArgsDelegateTypeAndFile?.type,
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

    private fun List<KSAnnotation>.getNavArgsDelegateType(
        composableName: String
    ): ReadNavArgsDelegateType.TypeWithFile? {
        var lastFound: ReadNavArgsDelegateType? = null

        forEach {
            it.getNavArgsDelegateType(composableName)?.let {
                lastFound = it
            }
        }

        return lastFound as? ReadNavArgsDelegateType.TypeWithFile?
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
        val navGraphAnnotation = relevantAnnotations.find { functionAnnotation ->
            val functionAnnotationType = functionAnnotation.annotationType.resolve()

            val didWeFindNavGraph = functionAnnotationType.declaration.annotations.any { annotationOfAnnotation ->
                annotationOfAnnotation.shortName.asString() == "NavGraph"
                        && annotationOfAnnotation.annotationType.resolve().declaration.qualifiedName?.asString() == NAV_GRAPH_ANNOTATION_QUALIFIED
            }

            if (didWeFindNavGraph) resolvedAnnotation = functionAnnotationType

            didWeFindNavGraph
        }

        return if (navGraphAnnotation == null) {
            relevantAnnotations.mapNotNull {
                it.annotationType.resolve().declaration.getNavGraphInfo()
            }.firstOrNull()
        } else {
            NavGraphInfo.AnnotatedSource(
                start = navGraphAnnotation.arguments.first().value as Boolean,
                graphType = Importable(
                    resolvedAnnotation!!.declaration.simpleName.asString(),
                    resolvedAnnotation!!.declaration.qualifiedName!!.asString()
                )
            )
        }
    }

    private fun getDefaultNavGraphInfo(
        destinationAnnotations: List<KSAnnotation>,
        isActivityDestination: Boolean = false
    ): NavGraphInfo {
        return if (isActivityDestination) {
            NavGraphInfo.AnnotatedSource(false, rootNavGraphType)
        } else {
            NavGraphInfo.Legacy(
                start = destinationAnnotations.findOverridingArgumentValue { findArgumentValue<Boolean>(DESTINATION_ANNOTATION_START_ARGUMENT) }!!,
                navGraphRoute = destinationAnnotations.findOverridingArgumentValue { findArgumentValue<String>(DESTINATION_ANNOTATION_NAV_GRAPH_ARGUMENT) }!!,
            )
        }
    }

    sealed interface ReadNavArgsDelegateType {
        object Nothing: ReadNavArgsDelegateType
        class TypeWithFile(val type: NavArgsDelegateType?, val file: KSFile?): ReadNavArgsDelegateType
    }

    private fun KSAnnotation.getNavArgsDelegateType(
        composableName: String
    ): ReadNavArgsDelegateType? = kotlin.runCatching {
        val ksType = findArgumentValue<KSType>(DESTINATION_ANNOTATION_NAV_ARGS_DELEGATE_ARGUMENT)
            ?: return null

        val ksClassDeclaration = ksType.declaration as KSClassDeclaration
        if (ksClassDeclaration.isNothing) {
            return ReadNavArgsDelegateType.Nothing
        }

        val parameters = ksClassDeclaration.primaryConstructor!!
            .parameters
            .map { it.toParameter(composableName) }

        return ReadNavArgsDelegateType.TypeWithFile(
            NavArgsDelegateType(
                parameters,
                Importable(
                    ksClassDeclaration.simpleName.asString(),
                    ksClassDeclaration.qualifiedName!!.asString(),
                )
            ),
            ksClassDeclaration.containingFile
        )
    }.getOrElse {
        throw IllegalDestinationsSetup("There was an issue with '$DESTINATION_ANNOTATION_NAV_ARGS_DELEGATE_ARGUMENT'" +
                " of composable '$composableName': make sure it is a class with a primary constructor.", it)
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

    private fun KSAnnotation.toDeepLink(): DeepLink {
        return DeepLink(
            findArgumentValue("action")!!,
            findArgumentValue("mimeType")!!,
            findArgumentValue("uriPattern")!!,
        )
    }

    private fun KSType.toType(location: Location): TypeInfo? {
        val qualifiedName = declaration.qualifiedName ?: return null

        val ksClassDeclaration = findActualClassDeclaration()
        val classDeclarationType = ksClassDeclaration?.asType(emptyList())

        val importable = Importable(
            ksClassDeclaration?.simpleName?.asString() ?: declaration.simpleName.asString(),
            ksClassDeclaration?.qualifiedName?.asString() ?: qualifiedName.asString()
        )

        val typeArgs: List<TypeArgument> = if (declaration is KSTypeAlias) {
            val aliasTypes = (declaration as KSTypeAlias).type.resolve()
                .argumentTypes((declaration as KSTypeAlias).location)

            if (aliasTypes.any { it is TypeArgument.GenericType }) {
                val siteArgs = argumentTypes(location).toMutableList()

                aliasTypes.toMutableList().apply {
                    mapIndexed { idx, it ->
                        if (it is TypeArgument.GenericType) {
                            this[idx] = siteArgs.removeAt(0)
                        }
                    }
                }
            } else {
                aliasTypes
            }
        } else {
            argumentTypes(location)
        }

        return TypeInfo(
            value = Type(
                importable = importable,
                typeArguments = typeArgs,
                requireOptInAnnotations = ksClassDeclaration?.findAllRequireOptInAnnotations() ?: emptyList(),
                isEnum = ksClassDeclaration?.classKind == KSPClassKind.ENUM_CLASS,
                isParcelable = classDeclarationType?.let { parcelableType.isAssignableFrom(it) } ?: false,
                isSerializable = classDeclarationType?.let { serializableType.isAssignableFrom(it) } ?: false,
                isKtxSerializable = isKtxSerializable(),
                valueClassInnerInfo = ksClassDeclaration?.valueClassInnerInfo(),
            ),
            isNullable = isMarkedNullable,
            hasCustomTypeSerializer = navTypeSerializersByType[importable] != null,
        )
    }

    private fun KSClassDeclaration.valueClassInnerInfo(): ValueClassInnerInfo? {
        return if (modifiers.contains(Modifier.VALUE)) {
            // This is a value class, get the inner type's type only once (not recursively)
            val firstArg: KSValueParameter = primaryConstructor!!.parameters.first()
            val valueClassArgType = firstArg.type.resolve()

            val firstArgType = valueClassArgType.toType(valueClassArgType.declaration.location)
            val firstPublicNonNullableArgName = this.getDeclaredProperties().firstOrNull {
                it.isPublic() && it.simpleName.asString() == firstArg.name?.asString() && !it.type.resolve().isMarkedNullable
            }?.simpleName?.asString()

            firstArgType?.let {
                ValueClassInnerInfo(it, primaryConstructor!!.isPublic(), firstPublicNonNullableArgName)
            }
        } else {
            null
        }
    }

    private fun Sequence<KSAnnotation>.isKtxSerializable(): Boolean =
        any {
            it.annotationType.resolve().declaration.qualifiedName?.asString()
                ?.let { qualifiedName ->
                    qualifiedName == "kotlinx.serialization.Serializable"
                } ?: false
        }

    private fun KSType.isKtxSerializable(): Boolean {
        // Check current type annotations
        if (declaration.annotations.isKtxSerializable()) {
            return true
        }

        if (declaration is KSTypeAlias) {
            val typeAlias = declaration as KSTypeAlias
            // For alias check type alias annotations or annotations of reference type
            return typeAlias.type.annotations.isKtxSerializable() || typeAlias.type.resolve().isKtxSerializable()
        }

        return false
    }

    private fun KSType.argumentTypes(location: Location): List<TypeArgument> {
        return arguments.mapNotNull { typeArg ->
            if (typeArg.variance == Variance.STAR) {
                return@mapNotNull TypeArgument.Star
            }
            val resolvedType = typeArg.type?.resolve()

            if (resolvedType?.isError == true) {
                return@mapNotNull TypeArgument.Error(lazy { getErrorLines(location) })
            }

            if (resolvedType?.declaration is KSTypeParameter) return@mapNotNull TypeArgument.GenericType

            resolvedType?.toType(location)?.let { TypeArgument.Typed(it, typeArg.variance.label) }
        }
    }

    private fun KSValueParameter.toParameter(composableName: String): Parameter {
        val resolvedType = type.resolve()
        val type = resolvedType.toType(location)
            ?: throw IllegalDestinationsSetup("Parameter \"${name!!.asString()}\" of " +
                    "composable $composableName was not resolvable: please review it.")

        return Parameter(
            name = name!!.asString(),
            type = type,
            hasDefault = hasDefault,
            isMarkedNavHostParam = this.annotations.any {
                it.shortName.asString() == "NavHostParam" &&
                        it.annotationType.resolve().declaration.qualifiedName?.asString() == NAV_HOST_PARAM_ANNOTATION_QUALIFIED
            },
            lazyDefaultValue = lazy { getDefaultValue(resolver) }
        )
    }

    private fun getErrorLines(location: Location): String {
        val fileLocation = location as? FileLocation ?: return "NonExistentLocation"
        return File(fileLocation.filePath)
            .readLines(fileLocation.lineNumber, fileLocation.lineNumber + 1)
            .joinToString("")
    }


    private val defaultStyle by lazy {
        resolver.getClassDeclarationByName("$CORE_PACKAGE_NAME.spec.DestinationStyle.Default")!!
            .asType(emptyList())
    }

    private val bottomSheetStyle by lazy {
        resolver.getClassDeclarationByName("$CORE_PACKAGE_NAME.spec.DestinationStyleBottomSheet")?.asType(emptyList())
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

    private val parcelableType by lazy {
        resolver.getClassDeclarationByName("android.os.Parcelable")!!.asType(emptyList())
    }

    private val serializableType by lazy {
        resolver.getClassDeclarationByName("java.io.Serializable")!!.asType(emptyList())
    }

    private val activityType by lazy {
        resolver.getClassDeclarationByName("android.app.Activity")!!.asType(emptyList())
    }
}
