package com.ramcosta.composedestinations.codegen.model

import com.ramcosta.composedestinations.codegen.commons.sanitizePackageName

data class Importable(
    val simpleName: String,
    val qualifiedName: String
) {

    val preferredSimpleName = getCodeFriendlyName()
    internal val importStatement = getImportStatement().sanitizePackageName()

    private fun getImportStatement() = "import " + if (preferredSimpleName == simpleName) {
            qualifiedName
        } else {
            val suffixToRemove = preferredSimpleName.split(".").drop(1).joinToString(".")
            qualifiedName.removeSuffix(".$suffixToRemove")
        }

    private fun getCodeFriendlyName(): String {
        val result = StringBuilder()

        val splits = qualifiedName.split(".")
        if (!splits.last()[0].isUpperCase()) {
            // if last is not starting with uppercase, we don't do anything here
            return simpleName
        }

        for (part in splits) {
            if (part.firstOrNull()?.isUpperCase() == true) {
                if (result.isNotEmpty()) {
                    result.append('.')
                }
                result.append(part)
            }
        }

        return result.toString()
    }
}