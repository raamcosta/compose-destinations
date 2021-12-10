package com.ramcosta.composedestinations.codegen.model

data class Type(
    val classType: ClassType,
    val isNullable: Boolean,
    val isEnum: Boolean,
    val isParcelable: Boolean,
    val isSerializable: Boolean
) : ClassType by classType