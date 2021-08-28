package com.ramcosta.composedestinations.codegen.model

class Parameter(
    val name: String,
    val type: Type,
    val defaultValue: DefaultValue
) {
    val isMandatory: Boolean get() = !type.isNullable && defaultValue is DefaultValue.None
}