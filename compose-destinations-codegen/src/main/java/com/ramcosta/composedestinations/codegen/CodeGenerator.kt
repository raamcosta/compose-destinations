@file:Suppress("ObjectPropertyName")

package com.ramcosta.composedestinations.codegen

import com.ramcosta.composedestinations.codegen.commons.*
import com.ramcosta.composedestinations.codegen.facades.CodeOutputStreamMaker
import com.ramcosta.composedestinations.codegen.facades.Logger
import com.ramcosta.composedestinations.codegen.model.*
import com.ramcosta.composedestinations.codegen.servicelocator.*
import java.util.*

private var _generatedDestination: String? = null
private var _generatedNoArgsDestination: String? = null

internal lateinit var codeGenBasePackageName: String
internal lateinit var moduleName: String
internal val codeGenDestination get() = _generatedDestination ?: CORE_DESTINATION_SPEC
internal val codeGenNoArgsDestination get() = _generatedNoArgsDestination ?: CORE_DIRECTION_DESTINATION_SPEC

class CodeGenerator(
    override val logger: Logger,
    override val codeGenerator: CodeOutputStreamMaker,
    override val core: Core,
    override val codeGenConfig: CodeGenConfig
) : ServiceLocatorAccessor {

    fun generate(
        destinations: List<RawDestinationGenParams>,
        navGraphs: List<RawNavGraphGenParams>,
        navTypeSerializers: List<NavTypeSerializer>
    ) {
        initialValidator.validate(navGraphs, destinations)

        val shouldWriteSealedDestinations =  codeGenConfig.mode is CodeGenMode.SingleModule || destinations.size > 1
        initConfigurationValues(destinations, shouldWriteSealedDestinations)

        val destinationsWithNavArgs = destinationWithNavArgsMapper.map(destinations)

        val navTypeNamesByType = customNavTypeWriter.write(destinationsWithNavArgs, navTypeSerializers)

        val generatedDestinations = destinationsWriter(navTypeNamesByType)
            .write(destinationsWithNavArgs)

        moduleOutputWriter.write(navGraphs, generatedDestinations)

        if (shouldWriteSealedDestinations) {
            sealedDestinationWriter.write()
        }

        if (shouldWriteKtxSerializableNavTypeSerializer(destinations)) {
            defaultKtxSerializableNavTypeSerializerWriter.write()
        }
    }

    private fun initConfigurationValues(
        destinations: List<DestinationGeneratingParams>,
        shouldWriteSealedDestinations: Boolean
    ) {
        codeGenBasePackageName = codeGenConfig.packageName ?: destinations.getCommonPackageNamePart()
        moduleName = codeGenConfig.moduleName?.replaceFirstChar { it.uppercase(Locale.US) } ?: ""

        if (shouldWriteSealedDestinations) {
            _generatedDestination = moduleName + NO_PREFIX_GENERATED_DESTINATION
            _generatedNoArgsDestination = moduleName + NO_PREFIX_GENERATED_NO_ARGS_DESTINATION
        }
    }

    private fun shouldWriteKtxSerializableNavTypeSerializer(
        destinations: List<DestinationGeneratingParams>,
    ) = destinations.any {
        it.parameters.any { param ->
            param.type.run {
                isKtxSerializable &&
                        !hasCustomTypeSerializer &&
                        !isParcelable &&
                        !isSerializable
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
                """.trimIndent())
            }
    }
}
