package com.ramcosta.composedestinations.codegen.model

data class Parameter(
    val name: String,
    val type: Type,
    val defaultValue: DefaultValue?
) {
    val hasDefault get() = defaultValue != null

    val isMandatory: Boolean get() = !type.isNullable && !hasDefault
}