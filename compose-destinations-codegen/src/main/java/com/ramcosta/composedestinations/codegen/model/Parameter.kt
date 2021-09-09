package com.ramcosta.composedestinations.codegen.model

data class Parameter(
    val name: String,
    val type: Type,
    val defaultValueSrc: String?
) {
    val hasDefault get() = defaultValueSrc != null

    val isMandatory: Boolean get() = !type.isNullable && !hasDefault
}