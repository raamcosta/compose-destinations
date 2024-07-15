package com.ramcosta.composedestinations.codegen.model

data class CodeGenConfig(
    val packageName: String?,
    val moduleName: String?,
    val generateNavGraphs: Boolean,
    val htmlMermaidGraph: String?,
    val mermaidGraph: String?,
    val debugModeOutputDir: String?,
)
