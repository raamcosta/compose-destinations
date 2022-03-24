package com.ramcosta.composedestinations.codegen.model

data class NavTypeSerializer(
    val classKind: ClassKind,
    val serializerType: ClassType,
    val genericType: ClassType,
)
