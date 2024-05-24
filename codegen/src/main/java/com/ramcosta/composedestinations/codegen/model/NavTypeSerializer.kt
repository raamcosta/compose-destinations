package com.ramcosta.composedestinations.codegen.model

data class NavTypeSerializer(
    val classKind: ClassKind,
    val serializerType: Importable,
    val genericType: Importable,
)
