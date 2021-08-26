package com.ramcosta.composedestinations.processors

import com.ramcosta.composedestinations.facades.CodeOutputStreamMaker
import com.ramcosta.composedestinations.facades.Logger
import com.ramcosta.composedestinations.model.GeneratedDestinationFile
import com.ramcosta.composedestinations.templates.DESTINATIONS_COUNT
import com.ramcosta.composedestinations.templates.DESTINATIONS_INSIDE_MAP_OF
import com.ramcosta.composedestinations.templates.IMPORTS_BLOCK
import com.ramcosta.composedestinations.templates.destinationsTemplate
import com.ramcosta.composedestinations.utils.DESTINATIONS_AGGREGATE_CLASS
import com.ramcosta.composedestinations.utils.PACKAGE_NAME
import com.ramcosta.composedestinations.utils.plusAssign
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
            .replace(DESTINATIONS_INSIDE_MAP_OF, destinationsInsideMap(generatedDestinationFiles))

        file.close()
    }

    private fun importsCode(qualifiedNames: List<GeneratedDestinationFile>): String {
        val code = StringBuilder()
        qualifiedNames.forEachIndexed { i, it ->
            code.append("import ${it.qualifiedName}")
            if (i != qualifiedNames.lastIndex)
                code.append("\n")
        }

        return code.toString()
    }

    private fun destinationsInsideMap(destinations: List<GeneratedDestinationFile>): String {
        val code = StringBuilder()
        destinations.forEachIndexed { i, it ->
            code.append("${it.simpleName}.route to ${it.simpleName}")

            if (i != destinations.lastIndex)
                code.append(",\n\t\t")
        }

        return code.toString()
    }
}