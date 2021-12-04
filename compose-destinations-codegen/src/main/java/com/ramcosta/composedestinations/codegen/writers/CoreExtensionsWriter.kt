package com.ramcosta.composedestinations.codegen.writers

import com.ramcosta.composedestinations.codegen.commons.CORE_EXTENSIONS
import com.ramcosta.composedestinations.codegen.commons.PACKAGE_NAME
import com.ramcosta.composedestinations.codegen.commons.plusAssign
import com.ramcosta.composedestinations.codegen.facades.CodeOutputStreamMaker
import com.ramcosta.composedestinations.codegen.templates.coreExtensionsTemplate
import java.io.OutputStream

class CoreExtensionsWriter(
    private val codeGenerator: CodeOutputStreamMaker,
) {

    fun write() {
        val coreExtensions: OutputStream = codeGenerator.makeFile(
            packageName = PACKAGE_NAME,
            name = CORE_EXTENSIONS
        )

        coreExtensions += coreExtensionsTemplate

        coreExtensions.close()
    }
}