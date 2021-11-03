package com.ramcosta.composedestinations.codegen.model

data class Parameter(
    val name: String,
    val type: Type,
    val defaultValue: DefaultValue?,
    val variancePrefixes: List<String>,
    val argumentTypes: List<Type>
) {
    val hasDefault get() = defaultValue != null

    val isMandatory: Boolean get() = !type.isNullable && !hasDefault
}