package com.ramcosta.composedestinations.codegen

import com.ramcosta.composedestinations.codegen.commons.*
import com.ramcosta.composedestinations.codegen.facades.CodeOutputStreamMaker
import com.ramcosta.composedestinations.codegen.facades.Logger
import com.ramcosta.composedestinations.codegen.model.*
import com.ramcosta.composedestinations.codegen.servicelocator.*
import com.ramcosta.composedestinations.codegen.servicelocator.ServiceLocatorAccessor
import com.ramcosta.composedestinations.codegen.servicelocator.customNavTypeWriter
import com.ramcosta.composedestinations.codegen.servicelocator.destinationsWriter
import com.ramcosta.composedestinations.codegen.servicelocator.navGraphsObjectWriter

class CodeGenerator(
    override val logger: Logger,
    override val codeGenerator: CodeOutputStreamMaker,
    override val core: Core,
    override val generateNavGraphs: Boolean
): ServiceLocatorAccessor {

    fun generate(
        destinations: List<DestinationGeneratingParams>,
        navTypeSerializers: List<NavTypeSerializer>
    ) {
        initialValidations(destinations)

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

            if (!generateNavGraphs) {
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
}
