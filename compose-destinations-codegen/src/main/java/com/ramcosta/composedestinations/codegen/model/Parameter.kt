package com.ramcosta.composedestinations.codegen.model

class Parameter(
    val name: String,
    val type: Type,
    val hasDefault: Boolean,
    lazyDefaultValue: Lazy<DefaultValue?>,
) {
    val defaultValue: DefaultValue? by lazyDefaultValue

    val isMandatory: Boolean get() = !type.isNullable && !hasDefault

    override fun toString(): String {
        return "Parameter(name='$name', type=$type, hasDefault=$hasDefault)"
    }
}