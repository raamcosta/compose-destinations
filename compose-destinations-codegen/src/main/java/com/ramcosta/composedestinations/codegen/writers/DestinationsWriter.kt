package com.ramcosta.composedestinations.codegen.writers

import com.ramcosta.composedestinations.codegen.facades.CodeOutputStreamMaker
import com.ramcosta.composedestinations.codegen.facades.Logger
import com.ramcosta.composedestinations.codegen.model.*
import com.ramcosta.composedestinations.codegen.writers.sub.NavArgResolver

class DestinationsWriter(
    private val codeGenerator: CodeOutputStreamMaker,
    private val logger: Logger,
    private val core: Core,
    private val navArgResolver: NavArgResolver,
) {

    fun write(
        destinations: List<DestinationGeneratingParamsWithNavArgs>,
        navTypeNamesByType: Map<ClassType, CustomNavType>
    ): List<GeneratedDestination> {
        val generatedFiles = mutableListOf<GeneratedDestination>()

        destinations.forEach { destination ->
            val generatedDestination = SingleDestinationWriter(
                codeGenerator,
                logger,
                core,
                navArgResolver,
                destination,
                navTypeNamesByType
            ).write()

            generatedFiles.add(generatedDestination)
        }

        return generatedFiles
    }
}