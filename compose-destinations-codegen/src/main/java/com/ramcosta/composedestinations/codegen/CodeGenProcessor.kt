package com.ramcosta.composedestinations.codegen

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
        requireRoutesUniqueness(destinations)

        val generatedDestinations = DestinationsProcessor(codeGenerator, logger, availableDependencies).process(destinations)

        DestinationsObjectProcessor(codeGenerator, logger, availableDependencies).process(generatedDestinations)
    }

    private fun requireComposeNavigation() {
        if (!availableDependencies.composeNavigation) {
            throw MissingRequiredDependency("You must include the 'androidx.navigation:navigation-compose' dependency")
        }
    }

    private fun requireRoutesUniqueness(destinations: List<Destination>) {
        val cleanRoutes = mutableSetOf<String>()
        destinations.forEach {
            if (cleanRoutes.contains(it.cleanRoute)) {
                throw IllegalDestinationsSetup("Multiple @Destinations are using '${it.cleanRoute}' as its route name")
            }

            cleanRoutes.add(it.cleanRoute)
        }
    }
}
