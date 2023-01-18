package com.ramcosta.composedestinations.ksp

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.ramcosta.composedestinations.ksp.processors.Processor

/**
 * Processor provider which creates an instance of [Processor] with the available
 * [environment][SymbolProcessorEnvironment] parameters:
 * - [codeGenerator][SymbolProcessorEnvironment.codeGenerator]
 * - [logger][SymbolProcessorEnvironment.logger]
 * - [options][SymbolProcessorEnvironment.options]
 *
 * For more information, see [Processor]
 */
class ProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return Processor(
            environment.codeGenerator,
            environment.logger,
            environment.options
        )
    }
}