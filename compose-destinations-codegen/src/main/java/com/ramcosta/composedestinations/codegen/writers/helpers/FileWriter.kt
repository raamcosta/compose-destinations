package com.ramcosta.composedestinations.codegen.writers.helpers

import com.ramcosta.composedestinations.codegen.commons.plusAssign
import com.ramcosta.composedestinations.codegen.model.Importable
import com.ramcosta.composedestinations.codegen.templates.core.FileTemplate
import java.io.OutputStream

fun OutputStream.writeSourceFile(
    packageStatement: String,
    importableHelper: ImportableHelper,
    sourceCode: String,
    fileOptIns: Set<Importable> = emptySet()
) {
    this.use {
        it += "${fileOptInsCode(fileOptIns)}$packageStatement\n${importableHelper.addResolvedImportsToSrcCode(sourceCode)}"
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

private fun fileOptInsCode(
    fileOptIns: Set<Importable>,
): String {
    if (fileOptIns.isEmpty()) {
        return ""
    }
    return "@file:OptIn(${fileOptIns.joinToString(", ") { it.qualifiedName + "::class" }})\n\n"
}
