package com.ramcosta.composedestinations.codegen.commons

import com.ramcosta.composedestinations.codegen.model.*
import com.ramcosta.composedestinations.codegen.writers.helpers.ImportableHelper
import java.io.OutputStream
import java.util.*

operator fun OutputStream.plusAssign(str: String) {
    write(str.toByteArray())
}

operator fun StringBuilder.plusAssign(str: String) {
    append(str)
}

fun Type.recursiveRequireOptInAnnotations(): List<Importable> {
    val mutableList = requireOptInAnnotations.toMutableList()
    genericTypes.forEach {
        when (it) {
            is TypedGenericType -> mutableList.addAll(it.type.recursiveRequireOptInAnnotations())
            is ErrorGenericType,
            is StarGenericType -> Unit
        }
    }

    return mutableList
}

fun Type.toTypeCode(importableHelper: ImportableHelper? = null): String {
    val importableName = importableHelper?.addImportableAndGetPlaceholder(importable)
        ?: importable.simpleName

    if (genericTypes.isEmpty()) {
        return "${importableName}${if (isNullable) "?" else ""}"
    }

    return "${importableName}<${genericTypes.toTypesCode(importableHelper)}>${if (isNullable) "?" else ""}"
}

fun List<GenericType>.toTypesCode(importableHelper: ImportableHelper? = null): String {
    return joinToString(", ") { it.toTypeCode(importableHelper) }
}

fun GenericType.toTypeCode(importableHelper: ImportableHelper? = null): String {
    return when (this) {
        is StarGenericType -> varianceLabel
        is TypedGenericType -> "$varianceLabel${if(varianceLabel.isEmpty()) "" else " "}${type.toTypeCode(importableHelper)}"
        is ErrorGenericType -> "ERROR"
    }
}

fun Type.isPrimitive(): Boolean {
    return toPrimitiveNavTypeClassTypeOrNull() != null
}

val primitiveTypes = mapOf(
    String::class.qualifiedName to CORE_STRING_NAV_TYPE,
    Int::class.qualifiedName to CORE_INT_NAV_TYPE,
    Float::class.qualifiedName to CORE_FLOAT_NAV_TYPE,
    Long::class.qualifiedName to CORE_LONG_NAV_TYPE,
    Boolean::class.qualifiedName to CORE_BOOLEAN_NAV_TYPE
)

fun Type.toPrimitiveNavTypeClassTypeOrNull(): Importable? {
    return primitiveTypes[importable.qualifiedName]
}

fun Parameter.isComplexTypeNavArg(): Boolean {
    if (type.isEnum) {
        return false
    }

    return type.isParcelable ||
            (type.isSerializable && !type.isPrimitive()) ||
            type.hasCustomTypeSerializer ||
            type.isKtxSerializable
}

fun String.removeFromTo(from: String, to: String): String {
    val startIndex = indexOf(from)
    val endIndex = indexOf(to) + to.length

    return kotlin.runCatching { removeRange(startIndex, endIndex) }.getOrNull() ?: this
}

fun String.removeInstancesOf(vararg toRemove: String): String {
    var result = this
    toRemove.forEach {
        result = result.replace(it, "")
    }
    return result
}

private val humps = "(?<=.)(?=\\p{Upper})".toRegex()
fun String.toSnakeCase() = replace(humps, "_").lowercase(Locale.US)
