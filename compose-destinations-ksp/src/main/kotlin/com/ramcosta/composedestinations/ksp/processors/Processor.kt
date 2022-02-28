package com.ramcosta.composedestinations.ksp.processors

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.ramcosta.composedestinations.codegen.CodeGenerator
import com.ramcosta.composedestinations.codegen.commons.*
import com.ramcosta.composedestinations.codegen.model.*
import com.ramcosta.composedestinations.ksp.codegen.KspCodeOutputStreamMaker
import com.ramcosta.composedestinations.ksp.codegen.KspLogger
import com.ramcosta.composedestinations.ksp.commons.getNavTypeSerializers

class Processor(
    private val codeGenerator: KSPCodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>,
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val annotatedDestinations = resolver.getComposableDestinations()
        if (!annotatedDestinations.iterator().hasNext()) {
            return emptyList()
        }

        val kspLogger = KspLogger(logger)
        val functionsToDestinationsMapper = KspToCodeGenDestinationsMapper(resolver, kspLogger)
        val kspCodeOutputStreamMaker = KspCodeOutputStreamMaker(codeGenerator, functionsToDestinationsMapper)

        val destinations = functionsToDestinationsMapper.map(annotatedDestinations)
        val navTypeSerializers = resolver.getNavTypeSerializers()

        CodeGenerator(
            logger = kspLogger,
            codeGenerator = kspCodeOutputStreamMaker,
            core = resolver.getCoreType(),
            codeGenConfig = ConfigParser(logger, options).parse()
        ).generate(destinations, navTypeSerializers)

        return emptyList()
    }

    private fun Resolver.getComposableDestinations(): Sequence<KSFunctionDeclaration> {
        return getSymbolsWithAnnotation(DESTINATION_ANNOTATION_QUALIFIED)
            .filterIsInstance<KSFunctionDeclaration>()
    }

    private fun Resolver.getCoreType(): Core {
        val isUsingAnimationsCore = getClassDeclarationByName("$CORE_PACKAGE_NAME.animations.AnimatedNavHostEngine") != null

        return if (isUsingAnimationsCore) {
            Core.ANIMATIONS
        } else {
            Core.MAIN
        }
    }
}

typealias KSPClassKind = com.google.devtools.ksp.symbol.ClassKind
typealias KSPCodeGenerator = com.google.devtools.ksp.processing.CodeGenerator
