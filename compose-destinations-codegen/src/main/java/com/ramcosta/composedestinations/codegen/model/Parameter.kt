package com.ramcosta.composedestinations.codegen.model

class Parameter(
    val name: String,
    val type: Type,
    val isNullable: Boolean,
    val hasDefault: Boolean,
    lazyDefaultValue: Lazy<DefaultValue?>,
) {
    val defaultValue: DefaultValue? by lazyDefaultValue

    val isMandatory: Boolean get() = !isNullable && !hasDefault
}