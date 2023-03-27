package com.ramcosta.composedestinations.codegen.model

data class CodeGenConfig(
    val packageName: String?,
    val moduleName: String?,
    val mode: CodeGenMode,
    val useComposableVisibility: Boolean,
)

sealed class CodeGenMode {

    abstract fun shouldCreateSealedDestination(destinationSize: Int): Boolean

    object NavGraphs : CodeGenMode() {
        override fun shouldCreateSealedDestination(destinationSize: Int): Boolean {
            return destinationSize > 1
        }
    }

    object Destinations : CodeGenMode() {
        override fun shouldCreateSealedDestination(destinationSize: Int): Boolean {
            return destinationSize > 0
        }
    }

    class SingleModule(
        val generateNavGraphs: Boolean,
    ) : CodeGenMode() {
        override fun shouldCreateSealedDestination(destinationSize: Int): Boolean {
            return true
        }
    }
}