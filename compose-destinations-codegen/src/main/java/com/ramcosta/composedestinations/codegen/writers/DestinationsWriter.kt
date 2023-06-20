package com.ramcosta.composedestinations.codegen.writers

import com.ramcosta.composedestinations.codegen.facades.CodeOutputStreamMaker
import com.ramcosta.composedestinations.codegen.model.CodeGenConfig
import com.ramcosta.composedestinations.codegen.model.CodeGenProcessedDestination
import com.ramcosta.composedestinations.codegen.model.CustomNavType
import com.ramcosta.composedestinations.codegen.model.Type
import com.ramcosta.composedestinations.codegen.writers.helpers.ImportableHelper
import com.ramcosta.composedestinations.codegen.writers.helpers.NavArgResolver

internal class DestinationsWriter(
    private val codeGenConfig: CodeGenConfig,
    private val codeGenerator: CodeOutputStreamMaker,
    private val isBottomSheetDependencyPresent: Boolean,
    private val customNavTypeByType: Map<Type, CustomNavType>,
) {

    fun write(
        destinations: List<CodeGenProcessedDestination>,
    ) {

        destinations.forEach { destination ->
            val importableHelper = ImportableHelper()
            SingleDestinationWriter(
                codeGenConfig,
                codeGenerator,
                isBottomSheetDependencyPresent,
                NavArgResolver(customNavTypeByType, importableHelper),
                destination,
                importableHelper
            ).write()
        }
    }
}