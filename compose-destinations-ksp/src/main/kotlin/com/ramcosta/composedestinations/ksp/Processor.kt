package com.ramcosta.composedestinations.ksp

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.*
import com.ramcosta.composedestinations.codegen.commons.*
import com.ramcosta.composedestinations.codegen.model.DeepLink
import com.ramcosta.composedestinations.codegen.model.Destination
import com.ramcosta.composedestinations.codegen.model.Parameter
import com.ramcosta.composedestinations.codegen.model.Type
import com.ramcosta.composedestinations.codegen.processors.DestinationsObjectProcessor
import com.ramcosta.composedestinations.codegen.processors.DestinationsProcessor
import com.ramcosta.composedestinations.commons.findAnnotation
import com.ramcosta.composedestinations.commons.findArgumentValue
import com.ramcosta.composedestinations.commons.getDefaultValue
import kotlin.system.measureTimeMillis

internal class Processor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>,
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val annotatedFunctions: Sequence<KSFunctionDeclaration> =
            resolver.getSymbolsWithAnnotation(DESTINATION_ANNOTATION_QUALIFIED)
                .filterIsInstance<KSFunctionDeclaration>()

        if (!annotatedFunctions.iterator().hasNext()) {
            return emptyList()
        }

        val hasAccompanistAnimations: Boolean
        val took = measureTimeMillis {
            hasAccompanistAnimations = resolver.getClassDeclarationByName(resolver.getKSNameFromString("com.google.accompanist.navigation.animation.AnimatedComposeNavigator")) != null
        }
        logger.warn("hasAccompanistAnimations = ${hasAccompanistAnimations}, took $took ms")

        val hasScaffold: Boolean
        val took2 = measureTimeMillis {
            hasScaffold = resolver.getClassDeclarationByName(resolver.getKSNameFromString("androidx.compose.material.ScaffoldState")) != null
        }
        logger.warn("hasScaffold = ${hasScaffold}, took $took2 ms")

        val annotatedAnimationSpecs: Sequence<KSClassDeclaration> = resolver.getSymbolsWithAnnotation(DESTINATION_TRANSITIONS_SPEC_ANNOTATION_QUALIFIED)
            .filterIsInstance<KSClassDeclaration>()

        val animationsSpecByCleanRoute: Map<String, TransitionSpec> = annotatedAnimationSpecs
            .associate {
                val route = it.findAnnotation(DESTINATION_TRANSITIONS_SPEC_ANNOTATION)
                    .findArgumentValue<String>(DESTINATION_ANNOTATION_ROUTE_ARGUMENT)!!

                route to TransitionSpec(route, it.asType(emptyList()).toType(), it.containingFile)
            }

        val sourceFilesById = mutableMapOf<String, KSFile?>()
        val kspCodeOutputStreamMaker = KspCodeOutputStreamMaker(codeGenerator) { sourceFilesById[it] }
        val kspLogger = KspLogger(logger)

        val destinations = annotatedFunctions.map { ksFunction ->
            ksFunction.toDestination(animationsSpecByCleanRoute).also {
                sourceFilesById.addSourceFiles(ksFunction.containingFile!!, animationsSpecByCleanRoute[it.cleanRoute]?.file)
            }
        }

        val generatedDestinationFiles = DestinationsProcessor(
            kspCodeOutputStreamMaker,
            kspLogger
        ).process(destinations)

        DestinationsObjectProcessor(
            kspCodeOutputStreamMaker,
            kspLogger,
            hasScaffold,
            hasAccompanistAnimations
        ).process(generatedDestinationFiles)

        return emptyList()
    }

    private fun KSFunctionDeclaration.toDestination(transitionsSpecByRoute: Map<String, TransitionSpec>): Destination {
        val composableName = simpleName.asString()
        val name = composableName + GENERATED_DESTINATION_SUFFIX
        val destinationAnnotation = findAnnotation(DESTINATION_ANNOTATION)
        val deepLinksAnnotations = destinationAnnotation.findArgumentValue<ArrayList<KSAnnotation>>(DESTINATION_ANNOTATION_DEEP_LINKS)!!

        val cleanRoute = destinationAnnotation.findArgumentValue<String>(DESTINATION_ANNOTATION_ROUTE_ARGUMENT)!!
        val transitionSpec = transitionsSpecByRoute[cleanRoute]

        val sourceIds = if (transitionSpec?.file != null) {
            listOf(
                containingFile!!.fileName,
                transitionSpec.file.fileName
            )
        } else {
            listOf(containingFile!!.fileName)
        }

        return Destination(
            sourceIds = sourceIds,
            name = name,
            qualifiedName = "$PACKAGE_NAME.$name",
            composableName = composableName,
            composableQualifiedName = qualifiedName!!.asString(),
            cleanRoute = cleanRoute,
            transitionsSpecType = transitionSpec?.type,
            parameters = parameters.map { it.toParameter() },
            deepLinks = deepLinksAnnotations.map { it.toDeepLink() },
            isStart = destinationAnnotation.findArgumentValue<Boolean>(DESTINATION_ANNOTATION_START_ARGUMENT)!!,
            navGraphRoute = destinationAnnotation.findArgumentValue<String>(DESTINATION_ANNOTATION_NAV_GRAPH_ARGUMENT)!!,
            composableReceiverSimpleName = extensionReceiver?.toString()
        )
    }

    private fun KSValueParameter.toParameter(): Parameter {
        return Parameter(
            name!!.asString(),
            type.resolve().toType(),
            getDefaultValue()
        )
    }

    private fun KSType.toType() = Type(
        declaration.simpleName.asString(),
        declaration.qualifiedName!!.asString(),
        isMarkedNullable
    )

    private fun KSAnnotation.toDeepLink(): DeepLink {
        return DeepLink(
            findArgumentValue("action")!!,
            findArgumentValue("mimeType")!!,
            findArgumentValue("uriPattern")!!,
        )
    }

    private fun MutableMap<String, KSFile?>.addSourceFiles(
        composableFile: KSFile,
        transitionSpecFile: KSFile?,
    ) {
        this[composableFile.fileName] = composableFile

        if (transitionSpecFile != null) {
            this[transitionSpecFile.fileName] = transitionSpecFile
        }
    }

    private class TransitionSpec(
        val route: String,
        val type: Type,
        val file: KSFile?
    )
}