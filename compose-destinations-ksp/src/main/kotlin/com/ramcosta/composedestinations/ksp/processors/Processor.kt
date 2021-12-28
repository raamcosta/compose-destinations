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
import com.ramcosta.composedestinations.codegen.model.ClassKind
import com.ramcosta.composedestinations.codegen.model.ClassType
import com.ramcosta.composedestinations.codegen.model.Core
import com.ramcosta.composedestinations.codegen.model.NavTypeSerializer
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
        val navTypeSerializers = resolver.getNavTypeSerializers()

        CodeGenerator(
            logger = kspLogger,
            codeGenerator = kspCodeOutputStreamMaker,
            core = resolver.getCoreType(),
            generateNavGraphs = options["compose-destinations.generateNavGraphs"] != "false"
        ).generate(destinations, navTypeSerializers)

        return emptyList()
    }

    private fun Resolver.areExtensionsAlreadyGenerated(): Boolean {
        return getClassDeclarationByName("$PACKAGE_NAME.$GENERATED_DESTINATION") != null
    }

    private fun Resolver.getComposableDestinations(): Sequence<KSFunctionDeclaration> {
        return getSymbolsWithAnnotation(DESTINATION_ANNOTATION_QUALIFIED)
            .filterIsInstance<KSFunctionDeclaration>()
    }

    private fun Resolver.getNavTypeSerializers(): List<NavTypeSerializer> {
        return getSymbolsWithAnnotation(NAV_TYPE_SERIALIZER_ANNOTATION_QUALIFIED)
            .filterIsInstance<KSClassDeclaration>().map {
                if (it.classKind != KSPClassKind.CLASS && it.classKind != KSPClassKind.OBJECT) {
                    throw IllegalDestinationsSetup("${it.simpleName}: Type serializers must be either class or object!")
                }

                var superType: KSType? = null
                for (type in it.superTypes) {
                    val resolvedType = type.resolve()
                    if (resolvedType.declaration.qualifiedName?.asString() ==
                        "$PACKAGE_NAME.navargs.parcelable.ParcelableNavTypeSerializer") {
                        superType = resolvedType
                        break
                    }

                    if (resolvedType.declaration.qualifiedName?.asString() ==
                        "$PACKAGE_NAME.navargs.serializable.SerializableNavTypeSerializer") {
                        superType = resolvedType
                        break
                    }
                }
                if (superType == null) {
                    throw IllegalDestinationsSetup("${it.simpleName}: Type serializers must implement ParcelableNavTypeSerializer (or SerializableNavTypeSerializer for Serializable types)!")
                }
                val genericType = superType.arguments.first().type?.resolve()?.declaration as KSClassDeclaration

                NavTypeSerializer(
                    if (it.classKind == KSPClassKind.CLASS) ClassKind.CLASS else ClassKind.OBJECT,
                    ClassType(it.simpleName.asString(), it.qualifiedName!!.asString()),
                    ClassType(genericType.simpleName.asString(), genericType.qualifiedName!!.asString())
                )
            }.toList()
    }

    private fun Resolver.getCoreType(): Core {
        val isUsingAnimationsCore = getClassDeclarationByName("com.ramcosta.composedestinations.animations.AnimatedNavHostEngine") != null

        return if (isUsingAnimationsCore) {
            Core.ANIMATIONS
        } else {
            Core.MAIN
        }
    }
}

typealias KSPClassKind = com.google.devtools.ksp.symbol.ClassKind
typealias KSPCodeGenerator = com.google.devtools.ksp.processing.CodeGenerator