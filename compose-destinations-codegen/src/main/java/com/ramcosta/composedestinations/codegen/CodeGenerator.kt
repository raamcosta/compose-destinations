@file:Suppress("ObjectPropertyName")

package com.ramcosta.composedestinations.codegen

import com.ramcosta.composedestinations.codegen.commons.CORE_DESTINATION_SPEC
import com.ramcosta.composedestinations.codegen.commons.CORE_DIRECTION_DESTINATION_SPEC
import com.ramcosta.composedestinations.codegen.commons.NO_PREFIX_GENERATED_DESTINATION
import com.ramcosta.composedestinations.codegen.commons.NO_PREFIX_GENERATED_NO_ARGS_DESTINATION
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
        destinations: List<DestinationGeneratingParams>,
        navTypeSerializers: List<NavTypeSerializer>
    ) {
        initialValidator.validate(destinations)
        val shouldWriteSealedDestinations = shouldWriteSealedDestinations(destinations)
        initConfigurationValues(destinations, shouldWriteSealedDestinations)

        val destinationsWithNavArgs = destinationWithNavArgsMapper.map(destinations)

        val navTypeNamesByType = customNavTypeWriter.write(destinationsWithNavArgs, navTypeSerializers)

        val generatedDestinations = destinationsWriter.write(destinationsWithNavArgs, navTypeNamesByType)

        val generatedNavGraphs = writeForMode(generatedDestinations)

        if (codeGenConfig.mode is CodeGenMode.SingleModule) {
            coreExtensionsWriter.write(generatedNavGraphs)
        }

        if (shouldWriteSealedDestinations) {
            sealedDestinationWriter.write()
        }
    }

    private fun writeForMode(generatedDestinations: List<GeneratedDestination>): List<NavGraphGeneratingParams> {
        return when (codeGenConfig.mode) {
            is CodeGenMode.NavGraphs -> {
                navGraphsModeWriter.write(generatedDestinations)
                emptyList()
            }

            is CodeGenMode.Destinations -> {
                destinationsModeWriter.write(generatedDestinations)
                emptyList()
            }

            is CodeGenMode.SingleModule -> {
                if (codeGenConfig.mode.generateNavGraphs) {
                    navGraphsSingleObjectWriter.write(generatedDestinations)
                } else {
                    // We fallback to just generate a list of all destinations
                    destinationsModeWriter.write(generatedDestinations)
                    emptyList()
                }
            }
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

    private fun shouldWriteSealedDestinations(destinations: List<DestinationGeneratingParams>): Boolean {
        return codeGenConfig.mode is CodeGenMode.SingleModule || destinations.size > 1
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
    }
}
