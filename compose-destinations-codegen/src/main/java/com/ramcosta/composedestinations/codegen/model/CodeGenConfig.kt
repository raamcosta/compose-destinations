package com.ramcosta.composedestinations.codegen.model

data class CodeGenConfig(
    val packageName: String?,
    val moduleName: String?,
    val mode: CodeGenMode,
)

enum class CodeGenMode {
    NavGraphs, Destinations, SingleModule
}