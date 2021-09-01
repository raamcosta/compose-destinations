package com.ramcosta.composedestinations.codegen.processors

import com.ramcosta.composedestinations.codegen.facades.CodeOutputStreamMaker
import com.ramcosta.composedestinations.codegen.facades.Logger
import com.ramcosta.composedestinations.codegen.model.GeneratedDestinationFile
import com.ramcosta.composedestinations.codegen.commons.DESTINATIONS_AGGREGATE_CLASS
import com.ramcosta.composedestinations.codegen.commons.PACKAGE_NAME
import com.ramcosta.composedestinations.codegen.commons.plusAssign
import com.ramcosta.composedestinations.codegen.templates.DESTINATIONS_COUNT
import com.ramcosta.composedestinations.codegen.templates.DESTINATIONS_INSIDE_MAP_OF
import com.ramcosta.composedestinations.codegen.templates.IMPORTS_BLOCK
import com.ramcosta.composedestinations.codegen.templates.STARTING_DESTINATION
import com.ramcosta.composedestinations.codegen.templates.destinationsTemplate
import java.io.OutputStream

class DestinationsAggregateProcessor(
    private val codeGenerator: CodeOutputStreamMaker,
    private val logger: Logger
) {

    fun process(generatedDestinationFiles: List<GeneratedDestinationFile>) {
        val file: OutputStream = codeGenerator.makeFile(
            packageName = PACKAGE_NAME,
            name = DESTINATIONS_AGGREGATE_CLASS
        )

        file += destinationsTemplate
            .replace(IMPORTS_BLOCK, importsCode(generatedDestinationFiles))
            .replace(DESTINATIONS_COUNT, generatedDestinationFiles.size.toString())
            .replace(STARTING_DESTINATION, startingDestination(generatedDestinationFiles))
            .replace(DESTINATIONS_INSIDE_MAP_OF, destinationsInsideMap(generatedDestinationFiles))

        file.close()
    }

    private fun importsCode(qualifiedNames: List<GeneratedDestinationFile>): String {
        val code = StringBuilder()
        qualifiedNames.forEachIndexed { i, it ->
            code += "import ${it.qualifiedName}"
            if (i != qualifiedNames.lastIndex)
                code += "\n"
        }

        return code.toString()
    }

    private fun startingDestination(generatedDestinationFiles: List<GeneratedDestinationFile>): String {
        val startingDestinations = generatedDestinationFiles.filter { it.isStartDestination }
        if (startingDestinations.isEmpty()) {
            throw IllegalStateException("No start destination found!")
        }

        if (startingDestinations.size > 1) {
            throw IllegalStateException("Found ${startingDestinations.size} start destinations, only one is allowed!")
        }

        return startingDestinations[0].simpleName
    }

    private fun destinationsInsideMap(destinations: List<GeneratedDestinationFile>): String {
        val code = StringBuilder()
        destinations.forEachIndexed { i, it ->
            code += "${it.simpleName}.route to ${it.simpleName}"

            if (i != destinations.lastIndex)
                code += ",\n\t\t"
        }

        return code.toString()
    }
}