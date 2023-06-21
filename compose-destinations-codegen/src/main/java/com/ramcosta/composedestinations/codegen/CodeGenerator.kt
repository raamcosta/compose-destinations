@file:Suppress("ObjectPropertyName")

package com.ramcosta.composedestinations.codegen

import com.ramcosta.composedestinations.codegen.commons.*
import com.ramcosta.composedestinations.codegen.facades.CodeOutputStreamMaker
import com.ramcosta.composedestinations.codegen.model.*
import com.ramcosta.composedestinations.codegen.servicelocator.*
import java.util.*

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

        initConfigurationValues(destinations)

        val processedDestinations = destinationWithNavArgsMapper.map(destinations)

        val navTypeNamesByType = customNavTypeWriter.write(processedDestinations, navTypeSerializers)

        moduleOutputWriter(navTypeNamesByType).write(navGraphs, processedDestinations)

        destinationsWriter(navTypeNamesByType).write(processedDestinations)

        if (shouldWriteKtxSerializableNavTypeSerializer(processedDestinations)) {
            defaultKtxSerializableNavTypeSerializerWriter.write()
        }
    }

    private fun initConfigurationValues(
        destinations: List<DestinationGeneratingParams>
    ) {
        codeGenBasePackageName = (codeGenConfig.packageName ?: destinations.getCommonPackageNamePart()).sanitizePackageName()
        moduleName = codeGenConfig.moduleName?.replaceFirstChar { it.uppercase(Locale.US) } ?: ""
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

    private fun List<DestinationGeneratingParams>.getCommonPackageNamePart(): String {
        var currentCommonPackageName = ""
        map { it.composableQualifiedName }
            .forEachIndexed { idx, packageName ->
                if (idx == 0) {
                    currentCommonPackageName = packageName
                    return@forEachIndexed
                }
                currentCommonPackageName = currentCommonPackageName.commonPrefixWith(packageName)
            }

        if (!currentCommonPackageName.endsWith(".")) {
            currentCommonPackageName = currentCommonPackageName.split(".")
                .dropLast(1)
                .joinToString(".")
        }

        return currentCommonPackageName.removeSuffix(".")
            .ifEmpty {
                throw UnexpectedException(
                    """Unable to get package name for module. Please specify a package name to use in the module's build.gradle file with:"
                    ksp {
                        arg("compose-destinations.codeGenPackageName", "your.preferred.package.name")
                    }
                    And report this issue (with steps to reproduce) if possible. 
                """.trimIndent()
                )
            }
    }
}
