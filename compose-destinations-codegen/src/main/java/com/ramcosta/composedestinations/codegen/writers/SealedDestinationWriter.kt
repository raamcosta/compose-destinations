package com.ramcosta.composedestinations.codegen.writers

import com.ramcosta.composedestinations.codegen.codeGenBasePackageName
import com.ramcosta.composedestinations.codegen.facades.CodeOutputStreamMaker
import com.ramcosta.composedestinations.codegen.templates.sealedDestinationTemplate
import com.ramcosta.composedestinations.codegen.writers.helpers.writeSourceFile

class SealedDestinationWriter(
    private val codeGenerator: CodeOutputStreamMaker
) {

    fun write() {
        codeGenerator.makeFile(
            packageName = "$codeGenBasePackageName.destinations",
            name = "Destination"
        ).writeSourceFile(sealedDestinationTemplate)
    }
}