package com.ramcosta.composedestinations.codegen

import com.ramcosta.composedestinations.codegen.commons.*
import com.ramcosta.composedestinations.codegen.facades.CodeOutputStreamMaker
import com.ramcosta.composedestinations.codegen.facades.Logger
import com.ramcosta.composedestinations.codegen.model.*
import com.ramcosta.composedestinations.codegen.servicelocator.*

internal lateinit var codeGenBasePackageName: String

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
        initialValidations(destinations)

        codeGenBasePackageName = codeGenConfig.packageName ?: destinations.getCommonPackageNamePart()

        val destinationsWithNavArgs = destinationWithNavArgsMapper.map(destinations)

        val navTypeNamesByType = customNavTypeWriter.write(destinationsWithNavArgs, navTypeSerializers)

        val generatedDestinations = destinationsWriter.write(destinationsWithNavArgs, navTypeNamesByType)

        val generatedNavGraphs = navGraphsObjectWriter.write(generatedDestinations)

        coreExtensionsWriter.write(generatedNavGraphs)
        sealedDestinationWriter.write()
    }

    private fun initialValidations(destinations: List<DestinationGeneratingParams>) {
        val cleanRoutes = mutableListOf<String>()
        val composableNames = mutableListOf<String>()

        destinations.forEach { destination ->
            if (cleanRoutes.contains(destination.cleanRoute)) {
                throw IllegalDestinationsSetup("Multiple Destinations with '${destination.cleanRoute}' as its route name")
            }

            if (composableNames.contains(destination.composableName)) {
                throw IllegalDestinationsSetup("Destination composable names must be unique: found multiple named '${destination.composableName}'")
            }

            if (destination.composableReceiverSimpleName == COLUMN_SCOPE_SIMPLE_NAME) {
                if (core != Core.ANIMATIONS) {
                    throw IllegalDestinationsSetup("'${destination.composableName}' composable: You need to include $CORE_ANIMATIONS_DEPENDENCY dependency to use a $COLUMN_SCOPE_SIMPLE_NAME receiver!")
                }

                if (destination.destinationStyleType !is DestinationStyleType.BottomSheet) {
                    throw IllegalDestinationsSetup("'${destination.composableName}' composable: Only destinations with a DestinationStyle.BottomSheet style may have a $COLUMN_SCOPE_SIMPLE_NAME receiver!")
                }
            }

            if (destination.composableReceiverSimpleName == ANIMATED_VISIBILITY_SCOPE_SIMPLE_NAME) {
                if (core != Core.ANIMATIONS) {
                    throw IllegalDestinationsSetup("'${destination.composableName}' composable: You need to include $CORE_ANIMATIONS_DEPENDENCY dependency to use a $ANIMATED_VISIBILITY_SCOPE_SIMPLE_NAME receiver!")
                }

                if (destination.destinationStyleType is DestinationStyleType.Dialog || destination.destinationStyleType is DestinationStyleType.BottomSheet) {
                    throw IllegalDestinationsSetup("'${destination.composableName}' composable: Only destinations with a DestinationStyle.Animated or DestinationStyle.Default style may have a $ANIMATED_VISIBILITY_SCOPE_SIMPLE_NAME receiver!")
                }
            }

            if (!codeGenConfig.generateNavGraphs) {
                if (destination.navGraphRoute != "root") {
                    logger.warn("'${destination.composableName}' composable: a navGraph was set but it will be ignored. Reason: 'compose-destinations.generateNavGraphs' was set to false at ksp gradle configuration.")
                }

                if (destination.isStart) {
                    logger.warn("'${destination.composableName}' composable: destination was set as the start destination but that will be ignored. Reason: 'compose-destinations.generateNavGraphs' was set to false at ksp gradle configuration.")
                }
            }

            cleanRoutes.add(destination.cleanRoute)
            composableNames.add(destination.composableName)
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
