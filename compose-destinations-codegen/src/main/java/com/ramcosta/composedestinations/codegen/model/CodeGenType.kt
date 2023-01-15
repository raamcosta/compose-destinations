package com.ramcosta.composedestinations.codegen.model

interface CodeGenType {
    val importable: Importable
    val typeArguments: List<TypeArgument>
    val requireOptInAnnotations: List<Importable>
    val isEnum: Boolean
    val isParcelable: Boolean
    val isSerializable: Boolean
    val isKtxSerializable: Boolean
    val valueClassInnerInfo: ValueClassInnerInfo?
}

data class TypeInfo(
    val value: Type,
    val isNullable: Boolean,
    val hasCustomTypeSerializer: Boolean
): CodeGenType by value

data class Type(
    override val importable: Importable,
    override val typeArguments: List<TypeArgument>,
    override val requireOptInAnnotations: List<Importable>,
    override val isEnum: Boolean,
    override val isParcelable: Boolean,
    override val isSerializable: Boolean,
    override val isKtxSerializable: Boolean,
    override val valueClassInnerInfo: ValueClassInnerInfo?,
): CodeGenType

data class ValueClassInnerInfo(
    val typeInfo: TypeInfo,
    val isConstructorPublic: Boolean,
    val publicNonNullableField: String?
)
