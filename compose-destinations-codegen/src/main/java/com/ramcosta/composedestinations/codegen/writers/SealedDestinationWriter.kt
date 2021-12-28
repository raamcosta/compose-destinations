package com.ramcosta.composedestinations.codegen.writers

import com.ramcosta.composedestinations.codegen.commons.*
import com.ramcosta.composedestinations.codegen.facades.CodeOutputStreamMaker
import com.ramcosta.composedestinations.codegen.templates.*
import java.io.OutputStream

class SealedDestinationWriter(
    private val codeGenerator: CodeOutputStreamMaker
) {

    fun write() {
        val file: OutputStream = codeGenerator.makeFile(
            packageName = "$PACKAGE_NAME.destinations",
            name = "Destination"
        )

        file += sealedDestinationTemplate
        file.close()
    }
}