package com.ramcosta.composedestinations.codegen.model

data class Type(
    val classType: ClassType,
    val genericTypes: List<GenericType>,
    val requireOptInAnnotations: List<ClassType>,
    val isNullable: Boolean,
    val isEnum: Boolean,
    val isParcelable: Boolean,
    val isSerializable: Boolean,
    val hasCustomTypeSerializer: Boolean
)