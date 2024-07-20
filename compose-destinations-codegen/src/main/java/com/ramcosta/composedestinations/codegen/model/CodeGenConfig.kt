package com.ramcosta.composedestinations.codegen.model

import com.ramcosta.composedestinations.codegen.DEFAULT_GEN_PACKAGE_NAME
import com.ramcosta.composedestinations.codegen.commons.sanitizePackageName
import java.util.Locale
import java.util.UUID

data class CodeGenConfig(
    val packageName: String,
    val moduleName: String,
    val registryId: String,
    val generateNavGraphs: Boolean,
    val htmlMermaidGraph: String?,
    val mermaidGraph: String?,
    val debugModeOutputDir: String?,
    val isBottomSheetDependencyPresent: Boolean
) {

    init {
        com.ramcosta.composedestinations.codegen.codeGenBasePackageName = packageName
        com.ramcosta.composedestinations.codegen.moduleName = moduleName
        com.ramcosta.composedestinations.codegen.registryId = registryId
    }

    companion object {
        operator fun invoke(
            packageName: String?,
            moduleName: String?,
            generateNavGraphs: Boolean,
            htmlMermaidGraph: String?,
            mermaidGraph: String?,
            debugModeOutputDir: String?,
            isBottomSheetDependencyPresent: Boolean
        ): CodeGenConfig {
            val finalModuleName = moduleName?.replaceFirstChar { it.uppercase(Locale.US) } ?: ""
            val defaultPackageName = if (finalModuleName.isEmpty()) {
                DEFAULT_GEN_PACKAGE_NAME
            } else {
                "$DEFAULT_GEN_PACKAGE_NAME.${finalModuleName.lowercase()}".sanitizePackageName()
            }
            val codeGenBasePackageName = packageName?.sanitizePackageName() ?: defaultPackageName
            val registryId = finalModuleName.ifEmpty { UUID.randomUUID().toString().replace("-", "_") }

            return CodeGenConfig(
                packageName = codeGenBasePackageName,
                moduleName = finalModuleName,
                registryId = registryId,
                generateNavGraphs = generateNavGraphs,
                htmlMermaidGraph = htmlMermaidGraph,
                mermaidGraph = mermaidGraph,
                debugModeOutputDir = debugModeOutputDir,
                isBottomSheetDependencyPresent = isBottomSheetDependencyPresent
            )
        }
    }
}
