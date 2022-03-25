package com.ramcosta.composedestinations.ksp.processors

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.ramcosta.composedestinations.codegen.CodeGenerator
import com.ramcosta.composedestinations.codegen.commons.*
import com.ramcosta.composedestinations.codegen.model.*
import com.ramcosta.composedestinations.ksp.codegen.KspCodeOutputStreamMaker
import com.ramcosta.composedestinations.ksp.codegen.KspLogger

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
        val navTypeSerializers = resolver.getNavTypeSerializers()

        val kspLogger = KspLogger(logger)
        val functionsToDestinationsMapper = KspToCodeGenDestinationsMapper(
            resolver,
            kspLogger,
            navTypeSerializers.associateBy { it.genericType }
        )
        val kspCodeOutputStreamMaker = KspCodeOutputStreamMaker(codeGenerator, functionsToDestinationsMapper)

        val destinations = functionsToDestinationsMapper.map(annotatedDestinations)

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

    private fun Resolver.getNavTypeSerializers(): List<NavTypeSerializer> {
        return getSymbolsWithAnnotation(NAV_TYPE_SERIALIZER_ANNOTATION_QUALIFIED)
            .filterIsInstance<KSClassDeclaration>().map { serializer ->
                if (serializer.classKind != KSPClassKind.CLASS && serializer.classKind != KSPClassKind.OBJECT) {
                    throw IllegalDestinationsSetup("${serializer.simpleName}: Type serializers must be either class or object!")
                }

                var superType: KSType? = null
                for (type in serializer.superTypes) {
                    val resolvedType = type.resolve()
                    val resolvedTypeString = resolvedType.declaration.qualifiedName?.asString()
                    if (resolvedTypeString ==
                        "$CORE_PACKAGE_NAME.navargs.DestinationsNavTypeSerializer") {
                        superType = resolvedType
                        break
                    }
                }

                if (superType == null) {
                    throw IllegalDestinationsSetup("${serializer.simpleName}: Type serializers must implement DestinationsNavTypeSerializer!")
                }

                val genericType = superType.arguments.first().type?.resolve()?.declaration as KSClassDeclaration

                NavTypeSerializer(
                    classKind = if (serializer.classKind == KSPClassKind.CLASS) ClassKind.CLASS else ClassKind.OBJECT,
                    serializerType = ClassType(serializer.simpleName.asString(), serializer.qualifiedName!!.asString()),
                    genericType = ClassType(genericType.simpleName.asString(), genericType.qualifiedName!!.asString()),
                )
            }.toList()
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
