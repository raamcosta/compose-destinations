package com.ramcosta.composedestinations.codegen.model

class Parameter(
    val name: String,
    val type: Type,
    val hasDefault: Boolean,
    lazyDefaultValue: Lazy<DefaultValue?>,
) {
    val defaultValue: DefaultValue? by lazyDefaultValue

    val isMandatory: Boolean get() = !type.isNullable && !hasDefault

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Parameter

        return when {
            name != other.name -> false
            type != other.type -> false
            hasDefault != other.hasDefault -> false
            else -> true
        }
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + hasDefault.hashCode()
        return result
    }

    override fun toString(): String {
        return "Parameter(name='$name', type=$type, hasDefault=$hasDefault)"
    }
}