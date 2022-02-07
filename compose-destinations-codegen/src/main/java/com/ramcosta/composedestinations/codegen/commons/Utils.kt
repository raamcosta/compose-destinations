package com.ramcosta.composedestinations.codegen.commons

import com.ramcosta.composedestinations.codegen.model.*
import java.io.OutputStream
import java.util.*

operator fun OutputStream.plusAssign(str: String) {
    write(str.toByteArray())
}

operator fun StringBuilder.plusAssign(str: String) {
    append(str)
}

fun Type.recursiveRequireOptInAnnotations(): List<ClassType> {
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

fun Type.toTypeCode(): String {
    if (genericTypes.isEmpty()) {
        return "${classType.simpleName}${if (isNullable) "?" else ""}"
    }

    return "${classType.simpleName}<${genericTypes.toTypesCode()}>${if (isNullable) "?" else ""}"
}

fun List<GenericType>.toTypesCode(): String {
    return joinToString(", ") { it.toTypeCode() }
}

fun GenericType.toTypeCode(): String {
    return when (this) {
        is StarGenericType -> varianceLabel
        is TypedGenericType -> "$varianceLabel${if(varianceLabel.isEmpty()) "" else " "}${type.toTypeCode()}"
        is ErrorGenericType -> "ERROR"
    }
}

fun Type.isPrimitive(): Boolean {
    return toPrimitiveNavTypeCodeOrNull() != null
}

val primitiveTypes = mapOf(
    String::class.qualifiedName to CORE_STRING_NAV_TYPE,
    Int::class.qualifiedName to "NavType.IntType",
    Float::class.qualifiedName to "NavType.FloatType",
    Long::class.qualifiedName to "NavType.LongType",
    Boolean::class.qualifiedName to "NavType.BoolType",
)

fun Type.toPrimitiveNavTypeCodeOrNull(): String? {
    return primitiveTypes[classType.qualifiedName]
}

fun Parameter.isComplexTypeNavArg(): Boolean {
    return !type.isEnum
            && (type.isParcelable || (type.isSerializable && !type.isPrimitive()))
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