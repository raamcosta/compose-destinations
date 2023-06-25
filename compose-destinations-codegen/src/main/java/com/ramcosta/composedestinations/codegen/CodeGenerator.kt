@file:Suppress("ObjectPropertyName")

package com.ramcosta.composedestinations.codegen

import com.ramcosta.composedestinations.codegen.commons.*
import com.ramcosta.composedestinations.codegen.facades.CodeOutputStreamMaker
import com.ramcosta.composedestinations.codegen.model.*
import com.ramcosta.composedestinations.codegen.servicelocator.*
import java.util.*

private const val DEFAULT_GEN_PACKAGE_NAME = "com.ramcosta.composedestinations.generated"
internal lateinit var codeGenBasePackageName: String
internal lateinit var moduleName: String

class CodeGenerator(
    override val codeGenerator: CodeOutputStreamMaker,
    override val isBottomSheetDependencyPresent: Boolean,
    override val codeGenConfig: CodeGenConfig
) : ServiceLocatorAccessor {

    fun generate(
        destinations: List<RawDestinationGenParams>,
        navGraphs: List<RawNavGraphGenParams>,
        navTypeSerializers: List<NavTypeSerializer>
    ) {
        initialValidator.validate(navGraphs, destinations)

        initConfigurationValues()

        val processedDestinations = destinationWithNavArgsMapper.map(destinations)

        val navTypeNamesByType = customNavTypeWriter.write(processedDestinations, navTypeSerializers)

        moduleOutputWriter(navTypeNamesByType).write(navGraphs, processedDestinations)

        destinationsWriter(navTypeNamesByType).write(processedDestinations)

        if (shouldWriteKtxSerializableNavTypeSerializer(processedDestinations)) {
            defaultKtxSerializableNavTypeSerializerWriter.write()
        }
    }

    private fun initConfigurationValues() {
        moduleName = codeGenConfig.moduleName?.replaceFirstChar { it.uppercase(Locale.US) } ?: ""
        val defaultPackageName = DEFAULT_GEN_PACKAGE_NAME + if (moduleName.isEmpty()) {
            ""
        } else {
            ".$moduleName".lowercase()
        }
        codeGenBasePackageName = codeGenConfig.packageName?.sanitizePackageName() ?: defaultPackageName
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
