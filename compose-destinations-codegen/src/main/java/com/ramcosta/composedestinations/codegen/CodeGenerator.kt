package com.ramcosta.composedestinations.codegen

import com.ramcosta.composedestinations.codegen.commons.firstTypeInfoArg
import com.ramcosta.composedestinations.codegen.commons.isCustomArrayOrArrayListTypeNavArg
import com.ramcosta.composedestinations.codegen.facades.CodeOutputStreamMaker
import com.ramcosta.composedestinations.codegen.facades.Logger
import com.ramcosta.composedestinations.codegen.facades.debug
import com.ramcosta.composedestinations.codegen.model.CodeGenConfig
import com.ramcosta.composedestinations.codegen.model.CodeGenProcessedDestination
import com.ramcosta.composedestinations.codegen.model.NavTypeSerializer
import com.ramcosta.composedestinations.codegen.model.RawDestinationGenParams
import com.ramcosta.composedestinations.codegen.model.RawNavGraphGenParams
import com.ramcosta.composedestinations.codegen.model.SubModuleInfo
import com.ramcosta.composedestinations.codegen.servicelocator.ServiceLocator
import com.ramcosta.composedestinations.codegen.servicelocator.customNavTypeWriter
import com.ramcosta.composedestinations.codegen.servicelocator.defaultKtxSerializableNavTypeSerializerWriter
import com.ramcosta.composedestinations.codegen.servicelocator.destinationsWriter
import com.ramcosta.composedestinations.codegen.servicelocator.initialValidator
import com.ramcosta.composedestinations.codegen.servicelocator.moduleOutputWriter

internal const val DEFAULT_GEN_PACKAGE_NAME = "com.ramcosta.composedestinations.generated"
internal lateinit var codeGenBasePackageName: String
internal lateinit var moduleName: String
internal lateinit var registryId: String

class CodeGenerator(
    override val codeGenerator: CodeOutputStreamMaker,
    override val codeGenConfig: CodeGenConfig
) : ServiceLocator {

    init {
        Logger.instance.debug {
            """
            |CodeGenerator init:
            |---------------------------------------
            |${pprint(codeGenConfig)}
            """.trimMargin()
        }
    }

    fun generate(
        destinations: List<RawDestinationGenParams>,
        navGraphs: List<RawNavGraphGenParams>,
        navTypeSerializers: List<NavTypeSerializer>,
        submodules: List<SubModuleInfo>
    ) {
        Logger.instance.debug {
            """
                |Code gen input:
                |---------------------------------------
                |navGraphs:
                |${pprint(navGraphs)}
                |---------------------------------------
                |destinations:
                |${pprint(destinations)}
                |---------------------------------------
                |submodules:
                |${pprint(submodules)}
                |---------------------------------------
                |navTypeSerializers:
                |${pprint(navTypeSerializers)}
            """.trimMargin()
        }

        val validatedDestinations: List<CodeGenProcessedDestination> = initialValidator.validate(
            navGraphs = navGraphs,
            destinations = destinations,
            submoduleResultSenders = submodules
                .flatMap { it.publicResultSenders }
                .associateBy { it.genDestinationQualifiedName }
        )

        postValidateGenerate(
            navGraphs = navGraphs,
            destinations = validatedDestinations,
            navTypeSerializers = navTypeSerializers,
            submodules = submodules
        )
    }

    private fun postValidateGenerate(
        navGraphs: List<RawNavGraphGenParams>,
        destinations: List<CodeGenProcessedDestination>,
        navTypeSerializers: List<NavTypeSerializer>,
        submodules: List<SubModuleInfo>
    ) {
        val navTypeNamesByType =
            customNavTypeWriter.write(navGraphs, destinations, navTypeSerializers)

        moduleOutputWriter(navTypeNamesByType, submodules).write(navGraphs, destinations)

        destinationsWriter(navTypeNamesByType, submodules).write(destinations)

        if (shouldWriteKtxSerializableNavTypeSerializer(destinations)) {
            defaultKtxSerializableNavTypeSerializerWriter.write()
        }
    }

    private fun shouldWriteKtxSerializableNavTypeSerializer(
        destinations: List<CodeGenProcessedDestination>,
    ) = destinations.any {
        it.navArgs.any { navArg ->
            if (navArg.type.isCustomArrayOrArrayListTypeNavArg()) {
               navArg.type.value.firstTypeInfoArg.run {
                   isKtxSerializable &&
                           !hasCustomTypeSerializer &&
                           !isParcelable &&
                           !isSerializable
               }
            } else {
                navArg.type.run {
                    isKtxSerializable &&
                            !hasCustomTypeSerializer &&
                            !isParcelable &&
                            !isSerializable
                }
            }
        }
    }
}
