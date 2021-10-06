package com.ramcosta.composedestinations.codegen.processors

import com.ramcosta.composedestinations.codegen.facades.CodeOutputStreamMaker
import com.ramcosta.composedestinations.codegen.facades.Logger
import com.ramcosta.composedestinations.codegen.model.*

class DestinationsProcessor(
    private val codeGenerator: CodeOutputStreamMaker,
    private val logger: Logger,
    private val availableDependencies: AvailableDependencies
) {

    fun process(destinations: List<Destination>): List<GeneratedDestination> {
        val generatedFiles = mutableListOf<GeneratedDestination>()

        destinations.forEach { destination ->
            val generatedDestination = SingleDestinationProcessor(
                codeGenerator,
                logger,
                availableDependencies,
                destination
            ).process()

            generatedFiles.add(generatedDestination)
        }

        return generatedFiles
    }
}