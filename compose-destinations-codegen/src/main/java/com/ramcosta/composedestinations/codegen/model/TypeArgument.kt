package com.ramcosta.composedestinations.codegen.model

sealed interface TypeArgument {

    data class Typed(
        val type: TypeInfo,
        val varianceLabel: String
    ): TypeArgument

    data class Error(
        val linesStr: String,
    ) : TypeArgument

    data object GenericType: TypeArgument

    data object Star : TypeArgument {
        const val varianceLabel = "*"
    }
}

