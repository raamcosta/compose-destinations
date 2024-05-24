package com.ramcosta.composedestinations.codegen.commons

import com.ramcosta.composedestinations.codegen.model.CodeGenType
import com.ramcosta.composedestinations.codegen.model.Importable
import com.ramcosta.composedestinations.codegen.model.Parameter
import com.ramcosta.composedestinations.codegen.model.Type
import com.ramcosta.composedestinations.codegen.model.TypeArgument
import com.ramcosta.composedestinations.codegen.model.TypeArgument.Star.varianceLabel
import com.ramcosta.composedestinations.codegen.model.TypeInfo
import com.ramcosta.composedestinations.codegen.model.Visibility
import com.ramcosta.composedestinations.codegen.writers.helpers.ImportableHelper
import java.io.Serializable
import kotlin.reflect.KClass

val coreTypes = mapOf(
    String::class.asType(Visibility.PUBLIC) to CORE_STRING_NAV_TYPE,
    Int::class.asType(Visibility.PUBLIC) to CORE_INT_NAV_TYPE,
    Float::class.asType(Visibility.PUBLIC) to CORE_FLOAT_NAV_TYPE,
    Long::class.asType(Visibility.PUBLIC) to CORE_LONG_NAV_TYPE,
    Boolean::class.asType(Visibility.PUBLIC) to CORE_BOOLEAN_NAV_TYPE,

    IntArray::class.asType(Visibility.PUBLIC) to CORE_INT_ARRAY_NAV_TYPE,
    FloatArray::class.asType(Visibility.PUBLIC) to CORE_FLOAT_ARRAY_NAV_TYPE,
    LongArray::class.asType(Visibility.PUBLIC) to CORE_LONG_ARRAY_NAV_TYPE,
    BooleanArray::class.asType(Visibility.PUBLIC) to CORE_BOOLEAN_ARRAY_NAV_TYPE,
    Array::class.asTypeWithArg(String::class, Visibility.PUBLIC) to CORE_STRING_ARRAY_NAV_TYPE,

    ArrayList::class.asTypeWithArg(Boolean::class, Visibility.PUBLIC) to CORE_BOOLEAN_ARRAY_LIST_NAV_TYPE,
    ArrayList::class.asTypeWithArg(Float::class, Visibility.PUBLIC) to CORE_FLOAT_ARRAY_LIST_NAV_TYPE,
    ArrayList::class.asTypeWithArg(Int::class, Visibility.PUBLIC) to CORE_INT_ARRAY_LIST_NAV_TYPE,
    ArrayList::class.asTypeWithArg(Long::class, Visibility.PUBLIC) to CORE_LONG_ARRAY_LIST_NAV_TYPE,
    ArrayList::class.asTypeWithArg(String::class, Visibility.PUBLIC) to CORE_STRING_ARRAY_LIST_NAV_TYPE,
)

fun TypeInfo.recursiveRequireOptInAnnotations(): List<Importable> {
    val mutableList = requireOptInAnnotations.toMutableList()
    typeArguments.forEach {
        when (it) {
            is TypeArgument.Typed -> mutableList.addAll(it.type.recursiveRequireOptInAnnotations())
            is TypeArgument.Error,
            is TypeArgument.GenericType,
            is TypeArgument.Star -> Unit
        }
    }

    return mutableList
}

fun TypeInfo.toTypeCode(importableHelper: ImportableHelper? = null): String {
    val importableName = importableHelper?.addAndGetPlaceholder(importable)
        ?: importable.simpleName

    if (typeArguments.isEmpty()) {
        return "${importableName}${if (isNullable) "?" else ""}"
    }

    return "${importableName}<${typeArguments.toTypesCode(importableHelper)}>${if (isNullable) "?" else ""}"
}

fun List<TypeArgument>.toTypesCode(importableHelper: ImportableHelper? = null): String {
    return joinToString(", ") { it.toTypeCode(importableHelper) }
}

fun TypeArgument.toTypeCode(importableHelper: ImportableHelper? = null): String {
    return when (this) {
        is TypeArgument.Star -> varianceLabel
        is TypeArgument.Typed -> "$varianceLabel${if (varianceLabel.isEmpty()) "" else " "}${
            type.toTypeCode(
                importableHelper
            )
        }"
        is TypeArgument.Error -> "ERROR"
        is TypeArgument.GenericType -> "GENERIC TYPE"
    }
}

fun TypeInfo.isCoreType(): Boolean {
    return toCoreNavTypeImportableOrNull() != null
}

fun TypeInfo.toCoreNavTypeImportableOrNull(): Importable? {
    return coreTypes[value]
}

fun Parameter.isCustomTypeNavArg(): Boolean {
    return type.isCustomTypeNavArg()
}

fun Parameter.isEnumTypeOrTypeArg(): Boolean {
    return type.isEnum || (type.isArrayOrArrayList() && type.value.firstTypeArg.isEnum)
}

fun TypeInfo.isCustomTypeNavArg(): Boolean {
    if (isCoreType()) {
        return false
    }

    if (isCustomArrayOrArrayListTypeNavArg()) {
        return true
    }

    if (typeArguments.isNotEmpty()) {
        // Since we are not array custom type, then we cannot handle other type args
        return false
    }

    return isEnum ||
            isParcelable ||
            isSerializable ||
            hasCustomTypeSerializer ||
            isKtxSerializable
}

fun CodeGenType.isCustomArrayOrArrayListTypeNavArg(): Boolean {
    if (!isArrayOrArrayList()) return false

    val typeArg = (typeArguments.firstOrNull() as? TypeArgument.Typed)?.type ?: return false

    return typeArg.isEnum ||
            typeArg.isParcelable ||
            typeArg.isSerializable ||
            typeArg.hasCustomTypeSerializer ||
            typeArg.isKtxSerializable
}

fun CodeGenType.isArrayOrArrayList(): Boolean {
    return isArray() || isArrayList()
}

fun CodeGenType.isArray(): Boolean {
    return importable.qualifiedName == Array::class.qualifiedName
}

fun CodeGenType.isArrayList(): Boolean {
    return importable.qualifiedName == ArrayList::class.qualifiedName
}

val Type.firstTypeArg get() = firstTypeInfoArg.value

val Type.firstTypeInfoArg get() = (typeArguments.first() as TypeArgument.Typed).type

private fun KClass<*>.asTypeWithArg(that: KClass<*>, visibility: Visibility) = Type(
    importable = Importable(
        this.simpleName!!,
        this.qualifiedName!!
    ),
    typeArguments = listOf(
        TypeArgument.Typed(
            type = TypeInfo(
                that.asType(visibility),
                isNullable = false,
                hasCustomTypeSerializer = false,
            ),
            varianceLabel = ""
        )
    ),
    requireOptInAnnotations = emptyList(),
    isEnum = false,
    isParcelable = false,
    isSerializable = Serializable::class.java.isAssignableFrom(this.javaObjectType),
    isKtxSerializable = false,
    valueClassInnerInfo = null,
    visibility = Visibility.PUBLIC,
)

private fun KClass<*>.asType(visibility: Visibility): Type {

    return Type(
        importable = Importable(
            simpleName!!,
            qualifiedName!!
        ),
        typeArguments = emptyList(),
        requireOptInAnnotations = emptyList(),
        isEnum = java.isEnum,
        isParcelable = false,
        isSerializable = Serializable::class.java.isAssignableFrom(this.javaObjectType),
        isKtxSerializable = false,
        valueClassInnerInfo = null,
        visibility = visibility,
    )
}
