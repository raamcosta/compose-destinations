package com.ramcosta.composedestinations.ksp.processors

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.*
import com.ramcosta.composedestinations.codegen.commons.*
import com.ramcosta.composedestinations.codegen.model.*
import com.ramcosta.composedestinations.ksp.codegen.KspLogger
import com.ramcosta.composedestinations.ksp.commons.*
import java.util.*

class KspToCodeGenDestinationsMapper(
    private val resolver: Resolver,
    private val logger: KspLogger
) : KSFileSourceMapper {

    private val humps = "(?<=.)(?=\\p{Upper})".toRegex()

    private val sourceFilesById = mutableMapOf<String, KSFile?>()

    fun map(composableDestinations: Sequence<KSFunctionDeclaration>): List<Destination> {
        return composableDestinations.mapToDestinations(
            onAddedDestination = { ksFunction ->
                sourceFilesById[ksFunction.containingFile!!.fileName] = ksFunction.containingFile
            }
        )
    }

    override fun mapToKSFile(sourceId: String): KSFile? {
        return sourceFilesById[sourceId]
    }

    private fun Sequence<KSFunctionDeclaration>.mapToDestinations(
        onAddedDestination: (ksFunction: KSFunctionDeclaration) -> Unit
    ): List<Destination> {
        return map { ksFunction ->
            ksFunction
                .toDestination()
                .also { onAddedDestination(ksFunction) }
        }.toList()
    }

    private fun KSFunctionDeclaration.toDestination(): Destination {
        val composableName = simpleName.asString()
        val name = composableName + GENERATED_DESTINATION_SUFFIX
        val destinationAnnotation = findAnnotation(DESTINATION_ANNOTATION)
        val deepLinksAnnotations = destinationAnnotation.findArgumentValue<ArrayList<KSAnnotation>>(DESTINATION_ANNOTATION_DEEP_LINKS_ARGUMENT)!!

        val cleanRoute = destinationAnnotation.prepareRoute(composableName)

        return Destination(
            sourceIds = listOf(containingFile!!.fileName),
            name = name,
            qualifiedName = "$PACKAGE_NAME.$name",
            composableName = composableName,
            composableQualifiedName = qualifiedName!!.asString(),
            cleanRoute = cleanRoute,
            destinationStyleType = destinationAnnotation.getDestinationStyleType(),
            parameters = parameters.map { it.toParameter() },
            deepLinks = deepLinksAnnotations.map { it.toDeepLink() },
            isStart = destinationAnnotation.findArgumentValue<Boolean>(DESTINATION_ANNOTATION_START_ARGUMENT)!!,
            navGraphRoute = destinationAnnotation.findArgumentValue<String>(DESTINATION_ANNOTATION_NAV_GRAPH_ARGUMENT)!!,
            composableReceiverSimpleName = extensionReceiver?.toString(),
            requireOptInAnnotationNames = findAllRequireOptInAnnotations()
        )
    }

    private fun KSAnnotation.getDestinationStyleType(): DestinationStyleType {
        val ksStyleType = findArgumentValue<KSType>(DESTINATION_ANNOTATION_STYLE_ARGUMENT)
            ?: return DestinationStyleType.Default

        val defaultStyle = resolver.getClassDeclarationByName("com.ramcosta.composedestinations.DestinationStyle.Default")!!
                .asType(emptyList())
        if (defaultStyle.isAssignableFrom(ksStyleType)) {
            return DestinationStyleType.Default
        }

        val bottomSheet = resolver.getClassDeclarationByName("com.ramcosta.composedestinations.DestinationStyle.BottomSheet")!!
                .asType(emptyList())
        if (bottomSheet.isAssignableFrom(ksStyleType)) {
            return DestinationStyleType.BottomSheet
        }

        val dialog = resolver.getClassDeclarationByName("com.ramcosta.composedestinations.DestinationStyle.Dialog")!!
                .asType(emptyList())
        if (dialog.isAssignableFrom(ksStyleType)) {
            return DestinationStyleType.Dialog(ksStyleType.toType())
        }

        return DestinationStyleType.Animated(ksStyleType.toType(), ksStyleType.declaration.findAllRequireOptInAnnotations())
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

    private fun KSType.toType() = Type(
        declaration.simpleName.asString(),
        declaration.qualifiedName!!.asString(),
        isMarkedNullable
    )

    private fun KSValueParameter.toParameter(): Parameter {
        return Parameter(
            name!!.asString(),
            type.resolve().toType(),
            getDefaultValue()
        )
    }

    private fun String.toSnakeCase() = replace(humps, "_").lowercase(Locale.getDefault())
}
