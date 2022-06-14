package com.ramcosta.composedestinations.codegen.writers

import com.ramcosta.composedestinations.codegen.facades.CodeOutputStreamMaker
import com.ramcosta.composedestinations.codegen.facades.Logger
import com.ramcosta.composedestinations.codegen.model.*
import com.ramcosta.composedestinations.codegen.writers.helpers.ImportableHelper
import com.ramcosta.composedestinations.codegen.writers.helpers.NavArgResolver

class DestinationsWriter(
    private val codeGenerator: CodeOutputStreamMaker,
    private val logger: Logger,
    private val core: Core,
    private val customNavTypeByType: Map<Type, CustomNavType>,
) {

    fun write(
        destinations: List<DestinationGeneratingParamsWithNavArgs>,
    ): List<GeneratedDestination> {
        val generatedFiles = mutableListOf<GeneratedDestination>()

        destinations.forEach { destination ->
            val importableHelper = ImportableHelper()
            val generatedDestination = SingleDestinationWriter(
                codeGenerator,
                logger,
                core,
                NavArgResolver(customNavTypeByType, importableHelper),
                destination,
                customNavTypeByType,
                importableHelper
            ).write()

            generatedFiles.add(generatedDestination)
        }

        return generatedFiles
    }
}