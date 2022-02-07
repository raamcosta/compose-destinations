package com.ramcosta.composedestinations.codegen

import com.ramcosta.composedestinations.codegen.commons.NO_PREFIX_GENERATED_DESTINATION
import com.ramcosta.composedestinations.codegen.commons.NO_PREFIX_GENERATED_NO_ARGS_DESTINATION
import com.ramcosta.composedestinations.codegen.facades.CodeOutputStreamMaker
import com.ramcosta.composedestinations.codegen.facades.Logger
import com.ramcosta.composedestinations.codegen.model.*
import com.ramcosta.composedestinations.codegen.servicelocator.*
import java.util.*

internal lateinit var codeGenBasePackageName: String
internal lateinit var moduleName: String
internal lateinit var generatedDestination: String
internal lateinit var generatedNoArgsDestination: String

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
        initConfigurationValues(destinations)

        val destinationsWithNavArgs = destinationWithNavArgsMapper.map(destinations)

        val navTypeNamesByType = customNavTypeWriter.write(destinationsWithNavArgs, navTypeSerializers)

        val generatedDestinations = destinationsWriter.write(destinationsWithNavArgs, navTypeNamesByType)

        val generatedNavGraphs = writeForMode(generatedDestinations)

        coreExtensionsWriter.write(generatedNavGraphs)
        sealedDestinationWriter.write()
    }

    private fun initConfigurationValues(destinations: List<DestinationGeneratingParams>) {
        codeGenBasePackageName = codeGenConfig.packageName ?: destinations.getCommonPackageNamePart()
        moduleName = codeGenConfig.moduleName?.replaceFirstChar { it.uppercase(Locale.US) } ?: ""
        generatedDestination = moduleName + NO_PREFIX_GENERATED_DESTINATION
        generatedNoArgsDestination = moduleName + NO_PREFIX_GENERATED_NO_ARGS_DESTINATION
    }

    private fun writeForMode(generatedDestinations: List<GeneratedDestination>): List<NavGraphGeneratingParams> {
        return when (codeGenConfig.mode) {
            CodeGenMode.NavGraphs -> {
                navGraphsModeWriter.write(generatedDestinations)
                emptyList()
            }

            CodeGenMode.Destinations -> {
                destinationsModeWriter.write(generatedDestinations)
                emptyList()
            }

            CodeGenMode.SingleModule -> defaultModeWriter.write(generatedDestinations)
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
    }
}
