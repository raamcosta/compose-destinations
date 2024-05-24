package com.ramcosta.composedestinations.codegen.model

sealed interface TypeArgument {

    data class Typed(
        val type: TypeInfo,
        val varianceLabel: String
    ): TypeArgument

    data class Error(
        private val lazyLineStr: Lazy<String>,
    ) : TypeArgument {
        val lineStr get() = lazyLineStr.value
    }

    data object GenericType: TypeArgument

    data object Star : TypeArgument {
        const val varianceLabel = "*"
    }
}

