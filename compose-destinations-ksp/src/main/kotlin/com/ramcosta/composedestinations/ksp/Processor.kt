package com.ramcosta.composedestinations.ksp

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.*
import com.ramcosta.composedestinations.codegen.commons.*
import com.ramcosta.composedestinations.codegen.model.*
import com.ramcosta.composedestinations.codegen.processors.DestinationsObjectProcessor
import com.ramcosta.composedestinations.codegen.processors.DestinationsProcessor
import com.ramcosta.composedestinations.commons.findAnnotation
import com.ramcosta.composedestinations.commons.findArgumentValue
import com.ramcosta.composedestinations.commons.getDefaultValue
import java.lang.RuntimeException

internal class Processor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>,
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        checkRequirements(resolver)

        val composableDestinations = resolver.getComposableDestinations()
        if (!composableDestinations.iterator().hasNext()) {
            return emptyList()
        }

        val sourceFilesById = mutableMapOf<String, KSFile?>()
        val kspCodeOutputStreamMaker = KspCodeOutputStreamMaker(codeGenerator) { sourceFilesById[it] }
        val kspLogger = KspLogger(logger)

        val transitionSpecByCleanRoute = resolver.getDestinationTransitionSpecs()
        val generatedDestinationFiles = generateDestinations(
            composableDestinations = composableDestinations,
            transitionSpecForCleanRoute = { transitionSpecByCleanRoute[it] },
            onAddedDestination = { ksFunction, cleanRoute ->
                sourceFilesById.addSourceFiles(ksFunction.containingFile!!, transitionSpecByCleanRoute[cleanRoute]?.file)
            },
            kspCodeOutputStreamMaker = kspCodeOutputStreamMaker,
            kspLogger = kspLogger
        )

        generateDestinationsObject(
            kspCodeOutputStreamMaker = kspCodeOutputStreamMaker,
            kspLogger = kspLogger,
            config = prepareConfig(resolver),
            generatedDestinationFiles = generatedDestinationFiles
        )

        return emptyList()
    }

    private fun generateDestinationsObject(
        kspCodeOutputStreamMaker: KspCodeOutputStreamMaker,
        kspLogger: KspLogger,
        config: ProcessingConfig,
        generatedDestinationFiles: List<GeneratedDestination>
    ) {
        DestinationsObjectProcessor(
            kspCodeOutputStreamMaker,
            kspLogger,
            config,
        ).process(generatedDestinationFiles)
    }

    private fun generateDestinations(
        composableDestinations: Sequence<KSFunctionDeclaration>,
        transitionSpecForCleanRoute: (cleanRoute: String) -> TransitionSpec?,
        onAddedDestination: (ksFunction: KSFunctionDeclaration, cleanRoute: String) -> Unit,
        kspCodeOutputStreamMaker: KspCodeOutputStreamMaker,
        kspLogger: KspLogger
    ): List<GeneratedDestination> {

        val destinations = composableDestinations.mapToDestinations(
            transitionSpecForCleanRoute = transitionSpecForCleanRoute,
            onAddedDestination = onAddedDestination
        )

        return DestinationsProcessor(
            kspCodeOutputStreamMaker,
            kspLogger
        ).process(destinations)
    }

    private fun Sequence<KSFunctionDeclaration>.mapToDestinations(
        transitionSpecForCleanRoute: (cleanRoute: String) -> TransitionSpec?,
        onAddedDestination: (ksFunction: KSFunctionDeclaration, cleanRoute: String) -> Unit
    ): Sequence<Destination> {
        return map { ksFunction ->
            ksFunction
                .toDestination(transitionSpecForCleanRoute)
                .also { onAddedDestination(ksFunction, it.cleanRoute) }
        }
    }

    private fun Resolver.getDestinationTransitionSpecs(): Map<String, TransitionSpec> {
        return getSymbolsWithAnnotation(DESTINATION_TRANSITIONS_SPEC_ANNOTATION_QUALIFIED)
            .filterIsInstance<KSClassDeclaration>()
            .associate {
                val route = it.findAnnotation(DESTINATION_TRANSITIONS_SPEC_ANNOTATION)
                    .findArgumentValue<String>(DESTINATION_ANNOTATION_ROUTE_ARGUMENT)!!

                route to TransitionSpec(route, it.asType(emptyList()).toType(), it.containingFile)
            }
    }

    private fun Resolver.getComposableDestinations(): Sequence<KSFunctionDeclaration> {
        return getSymbolsWithAnnotation(DESTINATION_ANNOTATION_QUALIFIED)
            .filterIsInstance<KSFunctionDeclaration>()
    }

    private fun checkRequirements(resolver: Resolver) {
        if (resolver.getClassDeclarationByName("androidx.navigation.NavHost") == null) {
            throw RuntimeException("You need androidx.navigation:navigation-compose")
        }
    }

    private fun prepareConfig(resolver: Resolver): ProcessingConfig {
        val hasAccompanistAnimations = resolver.getClassDeclarationByName("com.google.accompanist.navigation.animation.AnimatedComposeNavigator") != null
        val hasScaffold = resolver.getClassDeclarationByName("androidx.compose.material.ScaffoldState") != null

        return ProcessingConfig(
            hasAccompanistAnimations,
            hasScaffold
        )
    }

    private fun KSFunctionDeclaration.toDestination(transitionSpecForCleanRoute: (cleanRoute: String) -> TransitionSpec?): Destination {
        val composableName = simpleName.asString()
        val name = composableName + GENERATED_DESTINATION_SUFFIX
        val destinationAnnotation = findAnnotation(DESTINATION_ANNOTATION)
        val deepLinksAnnotations = destinationAnnotation.findArgumentValue<ArrayList<KSAnnotation>>(DESTINATION_ANNOTATION_DEEP_LINKS)!!

        val cleanRoute = destinationAnnotation.findArgumentValue<String>(DESTINATION_ANNOTATION_ROUTE_ARGUMENT)!!
        val transitionSpec = transitionSpecForCleanRoute(cleanRoute)

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