package com.ramcosta.composedestinations.codegen.writers.sub

import com.ramcosta.composedestinations.codegen.codeGenBasePackageName
import com.ramcosta.composedestinations.codegen.commons.SINGLE_MODULE_EXTENSIONS
import com.ramcosta.composedestinations.codegen.facades.CodeOutputStreamMaker
import com.ramcosta.composedestinations.codegen.templates.singleModuleExtensionsTemplate
import com.ramcosta.composedestinations.codegen.writers.helpers.ImportableHelper
import com.ramcosta.composedestinations.codegen.writers.helpers.writeSourceFile
import java.io.OutputStream

internal class SingleModuleExtensionsWriter(
    private val codeGenerator: CodeOutputStreamMaker,
) {

    private val importableHelper = ImportableHelper(singleModuleExtensionsTemplate.imports)

    fun write() {
        val coreExtensions: OutputStream = codeGenerator.makeFile(
            packageName = codeGenBasePackageName,
            name = SINGLE_MODULE_EXTENSIONS
        )
        coreExtensions.writeSourceFile(
            packageStatement = singleModuleExtensionsTemplate.packageStatement,
            importableHelper = importableHelper,
            sourceCode = singleModuleExtensionsTemplate.sourceCode
        )
    }
}