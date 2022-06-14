package com.ramcosta.composedestinations.codegen.writers.helpers

import com.ramcosta.composedestinations.codegen.commons.plusAssign
import com.ramcosta.composedestinations.codegen.templates.core.FileTemplate
import java.io.OutputStream

fun OutputStream.writeSourceFile(
    packageStatement: String,
    importableHelper: ImportableHelper,
    sourceCode: String,
) {
    this.use {
        it += "$packageStatement\n${importableHelper.addResolvedImportsToSrcCode(sourceCode)}"
    }
}

fun OutputStream.writeSourceFile(
    fileTemplate: FileTemplate
) {
    writeSourceFile(
        packageStatement = fileTemplate.packageStatement,
        importableHelper = ImportableHelper(fileTemplate.imports),
        sourceCode = fileTemplate.sourceCode
    )
}