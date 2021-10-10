package com.ramcosta.composedestinations.codegen.processors

import com.ramcosta.composedestinations.codegen.commons.CORE_EXTENSIONS
import com.ramcosta.composedestinations.codegen.commons.PACKAGE_NAME
import com.ramcosta.composedestinations.codegen.commons.plusAssign
import com.ramcosta.composedestinations.codegen.facades.CodeOutputStreamMaker
import com.ramcosta.composedestinations.codegen.templates.sealedDestinationTemplate
import java.io.OutputStream

class CoreExtensionsProcessor(
    private val codeGenerator: CodeOutputStreamMaker
) {

    fun process() {
        val sealedDestSpecFile: OutputStream = codeGenerator.makeFile(
            packageName = PACKAGE_NAME,
            name = CORE_EXTENSIONS
        )

        sealedDestSpecFile += sealedDestinationTemplate

        sealedDestSpecFile.close()
    }
}