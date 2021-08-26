package com.ramcosta.composedestinations.ksp

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.validate
import com.ramcosta.composedestinations.model.Destination
import com.ramcosta.composedestinations.model.Parameter
import com.ramcosta.composedestinations.model.Type
import com.ramcosta.composedestinations.processors.DestinationsAggregateProcessor
import com.ramcosta.composedestinations.processors.DestinationsProcessor
import com.ramcosta.composedestinations.utils.*
import com.ramcosta.composedestinations.utils.DESTINATION_ANNOTATION
import com.ramcosta.composedestinations.utils.DESTINATION_ANNOTATION_ROUTE_ARGUMENT
import com.ramcosta.composedestinations.utils.DESTINATION_DEFINITION_SUFFIX
import com.ramcosta.composedestinations.utils.PACKAGE_NAME
import com.ramcosta.composedestinations.utils.findAnnotation
import com.ramcosta.composedestinations.utils.findArgumentValue

class Processor(
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
        val name = composableName + DESTINATION_DEFINITION_SUFFIX
        val destinationAnnotation = findAnnotation(DESTINATION_ANNOTATION)

        var navControllerParameter: Parameter? = null
        var navBackStackEntryParameter: Parameter? = null
        val otherParameters = mutableListOf<Parameter>()

        parameters.forEach { param ->
            val ksType = param.type.resolve()
            if (navControllerParameter == null && ksType.isNavController()) {
                navControllerParameter = param.toParameter(ksType)
                return@forEach
            }

            if (navBackStackEntryParameter == null && ksType.isNavBackStackEntry()) {
                navBackStackEntryParameter = param.toParameter(ksType)
                return@forEach
            }

            otherParameters.add(param.toParameter(ksType))
        }

        return Destination(
            name = name,
            qualifiedName = "$PACKAGE_NAME.$name",
            composableName = composableName,
            composableQualifiedName = qualifiedName!!.asString(),
            cleanRoute = destinationAnnotation.findArgumentValue<String>(DESTINATION_ANNOTATION_ROUTE_ARGUMENT)!!,
            navParameters = otherParameters,
            navController = navControllerParameter,
            navBackStackEntry = navBackStackEntryParameter,
        )
    }

    private fun KSValueParameter.toParameter(ksType: KSType): Parameter {
        return Parameter(
            name!!.asString(),
            Type(
                ksType.declaration.simpleName.asString(),
                ksType.declaration.qualifiedName!!.asString(),
                ksType.isMarkedNullable
            ),
            getDefaultValue()
        )
    }

    private fun KSType.isNavController(): Boolean {
        return declaration.qualifiedName?.asString() == NAV_CONTROLLER_QUALIFIED_NAME
    }

    private fun KSType.isNavBackStackEntry(): Boolean {
        return declaration.qualifiedName?.asString() == NAV_BACK_STACK_ENTRY_QUALIFIED_NAME
    }
}