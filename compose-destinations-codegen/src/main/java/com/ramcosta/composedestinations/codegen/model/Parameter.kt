package com.ramcosta.composedestinations.codegen.model

class Parameter(
    val name: String,
    val type: TypeInfo,
    val hasDefault: Boolean,
    val isMarkedNavHostParam: Boolean,
    lazyDefaultValue: Lazy<DefaultValue?>,
) {
    val defaultValue: DefaultValue? by lazyDefaultValue

    val isMandatory: Boolean get() = !type.isNullable && !hasDefault

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Parameter

        if (name != other.name) return false
        if (type != other.type) return false
        if (hasDefault != other.hasDefault) return false
        if (isMarkedNavHostParam != other.isMarkedNavHostParam) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + hasDefault.hashCode()
        result = 31 * result + isMarkedNavHostParam.hashCode()
        return result
    }

    override fun toString(): String {
        return "Parameter(name='$name', type=$type, hasDefault=$hasDefault, markedNavHostParam=$isMarkedNavHostParam)"
    }
}