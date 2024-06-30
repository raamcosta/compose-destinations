package com.ramcosta.composedestinations.codegen.templates.core

import com.ramcosta.composedestinations.codegen.model.Importable

class FileTemplate(
    val packageStatement: String,
    val imports: Set<Importable>,
    val sourceCode: String
)

fun setOfImportable(vararg qualifiedNames: Any?): Set<Importable> {
    return qualifiedNames.mapNotNullTo(mutableSetOf()) {
        it ?: return@mapNotNullTo null

        if (it is String) {
            Importable(it.substring(it.lastIndexOf(".") + 1), it)
        } else {
            it as Importable
        }
    }
}
