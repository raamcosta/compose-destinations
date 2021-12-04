package com.ramcosta.composedestinations.codegen.writers

import com.ramcosta.composedestinations.codegen.facades.CodeOutputStreamMaker
import com.ramcosta.composedestinations.codegen.facades.Logger
import com.ramcosta.composedestinations.codegen.model.*

class DestinationsWriter(
    private val codeGenerator: CodeOutputStreamMaker,
    private val logger: Logger,
    private val core: Core
) {

    fun write(destinations: List<Destination>): List<GeneratedDestination> {
        val generatedFiles = mutableListOf<GeneratedDestination>()

        destinations.forEach { destination ->
            val generatedDestination = SingleDestinationWriter(
                codeGenerator,
                logger,
                core,
                destination
            ).write()

            generatedFiles.add(generatedDestination)
        }

        return generatedFiles
    }
}