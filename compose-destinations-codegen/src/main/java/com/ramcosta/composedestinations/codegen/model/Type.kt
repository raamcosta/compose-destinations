package com.ramcosta.composedestinations.codegen.model

data class Type(
    val simpleName: String,
    val qualifiedName: String,
    val isNullable: Boolean,
    val isEnum: Boolean,
    val isParcelable: Boolean,
    val isSerializable: Boolean
)