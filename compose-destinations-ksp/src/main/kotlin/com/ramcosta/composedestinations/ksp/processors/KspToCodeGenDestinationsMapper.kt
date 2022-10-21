package com.ramcosta.composedestinations.ksp.processors

import com.google.devtools.ksp.findActualType
import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.isInternal
import com.google.devtools.ksp.isPrivate
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.*
import com.ramcosta.composedestinations.codegen.commons.*
import com.ramcosta.composedestinations.codegen.model.*
import com.ramcosta.composedestinations.codegen.model.Visibility
import com.ramcosta.composedestinations.ksp.commons.*
import java.io.File

class KspToCodeGenDestinationsMapper(
    private val resolver: Resolver,
    private val navTypeSerializersByType: Map<Importable, NavTypeSerializer>,
) : KSFileSourceMapper {

    private val defaultStyle by lazy {
        resolver.getClassDeclarationByName("$CORE_PACKAGE_NAME.spec.DestinationStyle.Default")!!
            .asType(emptyList())
    }

    private val bottomSheetStyle by lazy {
        resolver.getClassDeclarationByName("$CORE_PACKAGE_NAME.spec.DestinationStyle.BottomSheet")!!.asType(emptyList())
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

    private val sourceFilesById = mutableMapOf<String, KSFile?>()

    fun map(composableDestinations: Sequence<KSFunctionDeclaration>): List<RawDestinationGenParams> {
        return composableDestinations.map { it.toDestination() }.toList()
    }

    override fun mapToKSFile(sourceId: String): KSFile? {
        return sourceFilesById[sourceId]
    }

    private fun KSFunctionDeclaration.toDestination(): RawDestinationGenParams {
        val composableName = simpleName.asString()
        val name = composableName + GENERATED_DESTINATION_SUFFIX
        val destinationAnnotation = findAnnotation(DESTINATION_ANNOTATION)
        val deepLinksAnnotations = destinationAnnotation.findArgumentValue<ArrayList<KSAnnotation>>(DESTINATION_ANNOTATION_DEEP_LINKS_ARGUMENT)!!

        val cleanRoute = destinationAnnotation.prepareRoute(composableName)

        val navArgsDelegateTypeAndFile = destinationAnnotation.getNavArgsDelegateType(composableName)?.also {
            sourceFilesById[it.second.fileName] = it.second
        }
        sourceFilesById[containingFile!!.fileName] = containingFile

        return RawDestinationGenParams(
            sourceIds = listOfNotNull(containingFile!!.fileName, navArgsDelegateTypeAndFile?.second?.fileName),
            name = name,
            composableName = composableName,
            composableQualifiedName = qualifiedName!!.asString(),
            visibility = getDestinationVisibility(),
            cleanRoute = cleanRoute,
            destinationStyleType = destinationAnnotation.getDestinationStyleType(composableName),
            parameters = parameters.map { it.toParameter(composableName) },
            deepLinks = deepLinksAnnotations.map { it.toDeepLink() },
            navGraphInfo = getNavGraphInfo(destinationAnnotation),
            composableReceiverSimpleName = extensionReceiver?.toString(),
            requireOptInAnnotationTypes = findAllRequireOptInAnnotations(),
            navArgsDelegateType = navArgsDelegateTypeAndFile?.first
        )
    }

    private fun KSFunctionDeclaration.getDestinationVisibility(): Visibility {
        if (isPrivate()) {
            throw IllegalDestinationsSetup("Composable functions annotated with @Destination cannot be private!")
        }

        return if (isInternal()) Visibility.INTERNAL else Visibility.PUBLIC
    }

    private fun KSFunctionDeclaration.getNavGraphInfo(destinationAnnotation: KSAnnotation): NavGraphInfo {
        var resolvedAnnotation: KSType? = null
        val navGraphAnnotation = annotations.find { functionAnnotation ->
            val annotationShortName = functionAnnotation.shortName.asString()
            if (annotationShortName == "Composable" || annotationShortName == "Destination") {
                return@find false
            }

            val functionAnnotationType = functionAnnotation.annotationType.resolve()
            functionAnnotationType.declaration.annotations.any { annotationOfAnnotation ->
                annotationOfAnnotation.shortName.asString() == "NavGraph"
                        && annotationOfAnnotation.annotationType.resolve().declaration.qualifiedName?.asString() == NAV_GRAPH_ANNOTATION_QUALIFIED
            }.also {
                if (it) resolvedAnnotation = functionAnnotationType
            }
        }
            ?: return NavGraphInfo.Legacy(
                start = destinationAnnotation.findArgumentValue<Boolean>(DESTINATION_ANNOTATION_START_ARGUMENT)!!,
                navGraphRoute = destinationAnnotation.findArgumentValue<String>(DESTINATION_ANNOTATION_NAV_GRAPH_ARGUMENT)!!,
            )

        return NavGraphInfo.AnnotatedSource(
            start = navGraphAnnotation.arguments.first().value as Boolean,
            graphType = Importable(
                resolvedAnnotation!!.declaration.simpleName.asString(),
                resolvedAnnotation!!.declaration.qualifiedName!!.asString()
            )
        )
    }

    private fun KSAnnotation.getNavArgsDelegateType(
        composableName: String
    ): Pair<NavArgsDelegateType?, KSFile>? = kotlin.runCatching {
        val ksType = findArgumentValue<KSType>(DESTINATION_ANNOTATION_NAV_ARGS_DELEGATE_ARGUMENT)!!

        val ksClassDeclaration = ksType.declaration as KSClassDeclaration
        if (ksClassDeclaration.qualifiedName?.asString() == "java.lang.Void") {
            //Nothing::class (which is the default) maps to Void java class here
            return null
        }

        val parameters = ksClassDeclaration.primaryConstructor!!
            .parameters
            .map { it.toParameter(composableName) }

        return Pair(
            NavArgsDelegateType(
                parameters,
                Importable(
                    ksClassDeclaration.simpleName.asString(),
                    ksClassDeclaration.qualifiedName!!.asString(),
                )
            ),
            ksClassDeclaration.containingFile!!
        )
    }.getOrElse {
        throw IllegalDestinationsSetup("There was an issue with '$DESTINATION_ANNOTATION_NAV_ARGS_DELEGATE_ARGUMENT'" +
                " of composable '$composableName': make sure it is a class with a primary constructor.", it)
    }

    private fun KSAnnotation.getDestinationStyleType(composableName: String): DestinationStyleType {
        val ksStyleType = findArgumentValue<KSType>(DESTINATION_ANNOTATION_STYLE_ARGUMENT)
            ?: return DestinationStyleType.Default

        if (defaultStyle.isAssignableFrom(ksStyleType)) {
            return DestinationStyleType.Default
        }

        if (bottomSheetStyle.isAssignableFrom(ksStyleType)) {
            return DestinationStyleType.BottomSheet
        }

        val type = ksStyleType.toType(location) ?: throw IllegalDestinationsSetup("Parameter $DESTINATION_ANNOTATION_STYLE_ARGUMENT of Destination annotation in composable $composableName was not resolvable: please review it.")

        if (dialogStyle.isAssignableFrom(ksStyleType)) {
            return DestinationStyleType.Dialog(type)
        }

        if (runtimeStyle.isAssignableFrom(ksStyleType)) {
            return DestinationStyleType.Runtime
        }

        //then it must be animated (since animated ones implement a generated interface, it would require multi step processing which can be avoided like this)
        return DestinationStyleType.Animated(type, ksStyleType.declaration.findAllRequireOptInAnnotations())
    }

    private fun KSAnnotation.prepareRoute(composableName: String): String {
        val cleanRoute = findArgumentValue<String>(DESTINATION_ANNOTATION_ROUTE_ARGUMENT)!!
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
        val ksClassDeclaration = getClassDeclaration()
        val classDeclarationType = ksClassDeclaration?.asType(emptyList())

        val importable = Importable(
            ksClassDeclaration?.simpleName?.asString() ?: declaration.simpleName.asString(),
            ksClassDeclaration?.qualifiedName?.asString() ?: qualifiedName.asString()
        )
        return TypeInfo(
            value = Type(
                importable = importable,
                typeArguments = argumentTypes(location),
                requireOptInAnnotations = ksClassDeclaration?.findAllRequireOptInAnnotations() ?: emptyList(),
                isEnum = ksClassDeclaration?.classKind == KSPClassKind.ENUM_CLASS,
                isParcelable = classDeclarationType?.let { parcelableType.isAssignableFrom(it) } ?: false,
                isSerializable = classDeclarationType?.let { serializableType.isAssignableFrom(it) } ?: false,
                isKtxSerializable = isKtxSerializable(),
            ),
            isNullable = isMarkedNullable,
            hasCustomTypeSerializer = navTypeSerializersByType[importable] != null,
        )
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
                return@mapNotNull TypeArgument.Error(lazy { getErrorLine(location) })
            }

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

    private fun getErrorLine(location: Location): String {
        val fileLocation = location as FileLocation
        return File(fileLocation.filePath).readLine(fileLocation.lineNumber)
    }

    private fun KSType.getClassDeclaration(): KSClassDeclaration? {
        if (this.declaration is KSTypeAlias) {
            return (this.declaration as KSTypeAlias).findActualType()
        }

        return declaration as? KSClassDeclaration?
    }
}
