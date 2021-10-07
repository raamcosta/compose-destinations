package com.ramcosta.composedestinations.codegen

import com.ramcosta.composedestinations.codegen.commons.COMPOSE_NAVIGATION
import com.ramcosta.composedestinations.codegen.commons.IllegalDestinationsSetup
import com.ramcosta.composedestinations.codegen.commons.MissingRequiredDependency
import com.ramcosta.composedestinations.codegen.facades.CodeOutputStreamMaker
import com.ramcosta.composedestinations.codegen.facades.Logger
import com.ramcosta.composedestinations.codegen.model.AvailableDependencies
import com.ramcosta.composedestinations.codegen.model.Destination
import com.ramcosta.composedestinations.codegen.processors.DestinationsObjectProcessor
import com.ramcosta.composedestinations.codegen.processors.DestinationsProcessor

class CodeGenProcessor(
    private val logger: Logger,
    private val codeGenerator: CodeOutputStreamMaker,
    private val availableDependencies: AvailableDependencies
) {

    init {
        requireComposeNavigation()
    }

    fun process(destinations: List<Destination>) {
        initialValidations(destinations)

        val generatedDestinations = DestinationsProcessor(codeGenerator, logger, availableDependencies).process(destinations)

        DestinationsObjectProcessor(codeGenerator, logger, availableDependencies).process(generatedDestinations)
    }

    private fun requireComposeNavigation() {
        if (!availableDependencies.composeNavigation) {
            throw MissingRequiredDependency("You must include the '$COMPOSE_NAVIGATION' dependency")
        }
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

            cleanRoutes.add(it.cleanRoute)
            composableNames.add(it.composableName)
        }
    }
}
