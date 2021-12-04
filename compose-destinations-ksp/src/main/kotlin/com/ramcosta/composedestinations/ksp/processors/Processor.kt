package com.ramcosta.composedestinations.ksp.processors

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.ramcosta.composedestinations.codegen.CodeGenerator
import com.ramcosta.composedestinations.codegen.commons.DESTINATION_ANNOTATION_QUALIFIED
import com.ramcosta.composedestinations.codegen.commons.GENERATED_DESTINATION
import com.ramcosta.composedestinations.codegen.commons.PACKAGE_NAME
import com.ramcosta.composedestinations.codegen.model.Core
import com.ramcosta.composedestinations.ksp.codegen.KspCodeOutputStreamMaker
import com.ramcosta.composedestinations.ksp.codegen.KspLogger

class Processor(
    private val codeGenerator: KSPCodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>,
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val annotatedDestinations = resolver.getComposableDestinations()
        if (!annotatedDestinations.iterator().hasNext() && resolver.areExtensionsAlreadyGenerated()) {
            return emptyList()
        }

        val kspLogger = KspLogger(logger)
        val functionsToDestinationsMapper = KspToCodeGenDestinationsMapper(resolver, kspLogger)
        val kspCodeOutputStreamMaker = KspCodeOutputStreamMaker(codeGenerator, functionsToDestinationsMapper)

        val destinations = functionsToDestinationsMapper.map(annotatedDestinations)
        CodeGenerator(
            logger = kspLogger,
            codeGenerator = kspCodeOutputStreamMaker,
            core = resolver.getAvailableDependencies()
        ).generate(destinations)

        return emptyList()
    }

    private fun Resolver.areExtensionsAlreadyGenerated(): Boolean {
        return getClassDeclarationByName("$PACKAGE_NAME.$GENERATED_DESTINATION") != null
    }

    private fun Resolver.getComposableDestinations(): Sequence<KSFunctionDeclaration> {
        return getSymbolsWithAnnotation(DESTINATION_ANNOTATION_QUALIFIED)
            .filterIsInstance<KSFunctionDeclaration>()
    }

    private fun Resolver.getAvailableDependencies(): Core {
        val isUsingAnimationsCore = getClassDeclarationByName("com.ramcosta.composedestinations.animations.AnimatedNavHostEngine") != null

        return if (isUsingAnimationsCore) {
            Core.ANIMATIONS
        } else {
            Core.MAIN
        }
    }
}

typealias KSPCodeGenerator = com.google.devtools.ksp.processing.CodeGenerator