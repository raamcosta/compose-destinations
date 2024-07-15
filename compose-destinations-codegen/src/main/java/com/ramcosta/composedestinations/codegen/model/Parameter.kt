package com.ramcosta.composedestinations.codegen.model

data class Parameter(
    val name: String,
    val type: TypeInfo,
    val hasDefault: Boolean,
    val isMarkedNavHostParam: Boolean,
    val defaultValue: DefaultValue?,
) {
    val isMandatory: Boolean get() = !type.isNullable && !hasDefault
}