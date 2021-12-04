package com.ramcosta.composedestinations.codegen

import com.ramcosta.composedestinations.codegen.commons.*
import com.ramcosta.composedestinations.codegen.facades.CodeOutputStreamMaker
import com.ramcosta.composedestinations.codegen.facades.Logger
import com.ramcosta.composedestinations.codegen.model.Core
import com.ramcosta.composedestinations.codegen.model.Destination
import com.ramcosta.composedestinations.codegen.model.DestinationStyleType
import com.ramcosta.composedestinations.codegen.writers.CoreExtensionsWriter
import com.ramcosta.composedestinations.codegen.writers.DestinationsWriter
import com.ramcosta.composedestinations.codegen.writers.NavGraphsObjectWriter

class CodeGenerator(
    private val logger: Logger,
    private val codeGenerator: CodeOutputStreamMaker,
    private val core: Core
) {

    fun generate(destinations: List<Destination>) {
        initialValidations(destinations)

        val generatedDestinations = DestinationsWriter(codeGenerator, logger, core).write(destinations)

        NavGraphsObjectWriter(codeGenerator, logger).write(generatedDestinations)

        CoreExtensionsWriter(codeGenerator).write()
    }

    private fun initialValidations(destinations: List<Destination>) {
        val cleanRoutes = mutableListOf<String>()
        val composableNames = mutableListOf<String>()

        destinations.forEach {
            if (cleanRoutes.contains(it.cleanRoute)) {
                throw IllegalDestinationsSetup("Multiple Destinations with '${it.cleanRoute}' as its route name")
            }

            if (composableNames.contains(it.composableName)) {
                throw IllegalDestinationsSetup("Destination composable names must be unique: found multiple named '${it.composableName}'")
            }

            if (it.composableReceiverSimpleName == COLUMN_SCOPE_SIMPLE_NAME) {
                if (core != Core.ANIMATIONS) {
                    throw IllegalDestinationsSetup("${it.composableName} composable: You need to include $CORE_ANIMATIONS_DEPENDENCY dependency to use a $COLUMN_SCOPE_SIMPLE_NAME receiver!")
                }

                if (it.destinationStyleType !is DestinationStyleType.BottomSheet) {
                    throw IllegalDestinationsSetup("${it.composableName} composable: Only destinations with a DestinationStyle.BottomSheet style may have a $COLUMN_SCOPE_SIMPLE_NAME receiver!")
                }
            }

            if (it.composableReceiverSimpleName == ANIMATED_VISIBILITY_SCOPE_SIMPLE_NAME) {
                if (core != Core.ANIMATIONS) {
                        throw IllegalDestinationsSetup("${it.composableName} composable: You need to include $CORE_ANIMATIONS_DEPENDENCY dependency to use a $ANIMATED_VISIBILITY_SCOPE_SIMPLE_NAME receiver!")
                }

                if (it.destinationStyleType is DestinationStyleType.Dialog || it.destinationStyleType is DestinationStyleType.BottomSheet) {
                    throw IllegalDestinationsSetup("${it.composableName} composable: Only destinations with a DestinationStyle.Animated or DestinationStyle.Default style may have a $ANIMATED_VISIBILITY_SCOPE_SIMPLE_NAME receiver!")
                }
            }

            cleanRoutes.add(it.cleanRoute)
            composableNames.add(it.composableName)
        }
    }
}
