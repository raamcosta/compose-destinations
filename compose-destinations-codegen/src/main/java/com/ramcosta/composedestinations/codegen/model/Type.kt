package com.ramcosta.composedestinations.codegen.model

data class Type(
    val classType: ClassType,
    val genericTypes: List<GenericType>,
    val isNullable: Boolean,
    val isEnum: Boolean,
    val isParcelable: Boolean,
    val isSerializable: Boolean
)