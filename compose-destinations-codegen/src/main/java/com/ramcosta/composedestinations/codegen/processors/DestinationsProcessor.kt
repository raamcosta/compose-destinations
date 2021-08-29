package com.ramcosta.composedestinations.codegen.processors

import com.ramcosta.composedestinations.codegen.facades.CodeOutputStreamMaker
import com.ramcosta.composedestinations.codegen.facades.Logger
import com.ramcosta.composedestinations.codegen.model.*

class DestinationsProcessor(
    private val codeGenerator: CodeOutputStreamMaker,
    private val logger: Logger
) {

    fun process(destinations: Sequence<Destination>): List<GeneratedDestinationFile> {
        val generatedFiles = mutableListOf<GeneratedDestinationFile>()

        destinations.forEach { destination ->
            generatedFiles.add(SingleDestinationProcessor(codeGenerator, logger, destination).process())
        }

        return generatedFiles
    }
}