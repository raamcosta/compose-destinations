package com.ramcosta.composedestinations.ksp

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import com.ramcosta.composedestinations.codegen.commons.*
import com.ramcosta.composedestinations.codegen.model.DeepLink
import com.ramcosta.composedestinations.codegen.model.Destination
import com.ramcosta.composedestinations.codegen.model.Parameter
import com.ramcosta.composedestinations.codegen.model.Type
import com.ramcosta.composedestinations.codegen.processors.DestinationsAggregateProcessor
import com.ramcosta.composedestinations.codegen.processors.DestinationsProcessor
import com.ramcosta.composedestinations.commons.*
import com.ramcosta.composedestinations.commons.findAnnotation
import com.ramcosta.composedestinations.commons.findArgumentValue

internal class Processor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val annotatedFunctions: Sequence<KSFunctionDeclaration> =
            resolver.getSymbolsWithAnnotation(DESTINATION_ANNOTATION_QUALIFIED)
                .filterIsInstance<KSFunctionDeclaration>()

        if (!annotatedFunctions.iterator().hasNext()) {
            return emptyList()
        }

        val kspCodeOutputStreamMaker = KspCodeOutputStreamMaker(codeGenerator)
        val kspLogger = KspLogger(logger)

        val generatedDestinationFiles = DestinationsProcessor(
            kspCodeOutputStreamMaker,
            kspLogger
        ).process(
            annotatedFunctions.map { it.toDestination() }
        )

        DestinationsAggregateProcessor(kspCodeOutputStreamMaker, kspLogger).process(generatedDestinationFiles)

        return annotatedFunctions.filterNot { it.validate() }.toList()
    }

    private fun KSFunctionDeclaration.toDestination(): Destination {
        val composableName = simpleName.asString()
        val name = composableName + GENERATED_DESTINATION_SUFFIX
        val destinationAnnotation = findAnnotation(DESTINATION_ANNOTATION)
        val deepLinksAnnotations = destinationAnnotation.findArgumentValue<ArrayList<KSAnnotation>>(DESTINATION_ANNOTATION_DEEP_LINKS)!!

        return Destination(
            name = name,
            qualifiedName = "$PACKAGE_NAME.$name",
            composableName = composableName,
            composableQualifiedName = qualifiedName!!.asString(),
            cleanRoute = destinationAnnotation.findArgumentValue<String>(DESTINATION_ANNOTATION_ROUTE_ARGUMENT)!!,
            parameters = parameters.map { it.toParameter() },
            deepLinks = deepLinksAnnotations.map { it.toDeepLink() },
            isStart = destinationAnnotation.findArgumentValue<Boolean>(DESTINATION_ANNOTATION_START_ARGUMENT)!!,
            navGraphName = destinationAnnotation.findArgumentValue<String>(DESTINATION_ANNOTATION_NAV_GRAPH_ARGUMENT)!!
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
}