package com.ramcosta.composedestinations.codegen.model

data class Type(
    val importable: Importable,
    val typeArguments: List<TypeArgument>,
    val requireOptInAnnotations: List<Importable>,
    val isNullable: Boolean,
    val isEnum: Boolean,
    val isParcelable: Boolean,
    val isSerializable: Boolean,
    val hasCustomTypeSerializer: Boolean,
    val isKtxSerializable: Boolean,
)
