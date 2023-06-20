package com.ramcosta.composedestinations.codegen.model

data class CodeGenConfig(
    val packageName: String?,
    val moduleName: String?,
    val mode: CodeGenMode,
)

sealed interface CodeGenMode {

    object NavGraphs : CodeGenMode

    object Destinations : CodeGenMode

    class SingleModule(
        val generateNavGraphs: Boolean,
    ) : CodeGenMode
}