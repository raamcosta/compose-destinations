package com.ramcosta.composedestinations.codegen.model

sealed interface GenericType

data class TypedGenericType(
    val type: Type,
    val varianceLabel: String
): GenericType

data class ErrorGenericType(
    private val lazyLineStr: Lazy<String>,
) : GenericType {
    val lineStr get() = lazyLineStr.value
}

object StarGenericType : GenericType {
    const val varianceLabel = "*"
}
