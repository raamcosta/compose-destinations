package com.ramcosta.composedestinations.ksp.processors

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.ramcosta.composedestinations.codegen.CodeGenerator
import com.ramcosta.composedestinations.codegen.commons.DESTINATION_ANNOTATION_QUALIFIED
import com.ramcosta.composedestinations.codegen.model.AvailableDependencies
import com.ramcosta.composedestinations.ksp.codegen.KspCodeOutputStreamMaker
import com.ramcosta.composedestinations.ksp.codegen.KspLogger

class Processor(
    private val codeGenerator: KSPCodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>,
) : SymbolProcessor {

    @OptIn(KspExperimental::class)
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val annotatedDestinations = resolver.getComposableDestinations()
        if (!annotatedDestinations.iterator().hasNext()) {
            return emptyList()
        }

        val kspLogger = KspLogger(logger)
        val functionsToDestinationsMapper = KspToCodeGenDestinationsMapper(resolver, kspLogger)
        val kspCodeOutputStreamMaker = KspCodeOutputStreamMaker(codeGenerator, functionsToDestinationsMapper)

        val destinations = functionsToDestinationsMapper.map(annotatedDestinations)
        CodeGenerator(
            kspLogger,
            kspCodeOutputStreamMaker,
            resolver.getAvailableDependencies()
        ).generate(destinations)

        return emptyList()
    }

    private fun Resolver.getComposableDestinations(): Sequence<KSFunctionDeclaration> {
        return getSymbolsWithAnnotation(DESTINATION_ANNOTATION_QUALIFIED)
            .filterIsInstance<KSFunctionDeclaration>()
    }

    private fun Resolver.getAvailableDependencies(): AvailableDependencies {
        val hasAccompanistAnimations = getClassDeclarationByName("com.google.accompanist.navigation.animation.AnimatedComposeNavigator") != null
        val hasScaffold = getClassDeclarationByName("androidx.compose.material.ScaffoldState") != null
        val hasComposeNavigation = getClassDeclarationByName("androidx.navigation.NavHost") != null
        val hasAccompanistMaterial = getClassDeclarationByName("com.google.accompanist.navigation.material.BottomSheetNavigator") != null

        return AvailableDependencies(
            hasComposeNavigation,
            hasScaffold,
            hasAccompanistAnimations,
            hasAccompanistMaterial
        )
    }
}

typealias KSPCodeGenerator = com.google.devtools.ksp.processing.CodeGenerator