package com.ramcosta.composedestinations.navargs.primitives

const val ENCODED_NULL = "%02null%03"
const val DECODED_NULL: String = "\u0002null\u0003"

const val encodedComma = "%2C"

@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
fun <E: Enum<*>> Class<E>.valueOfIgnoreCase(enumValueName: String): E {
    return enumConstants.firstOrNull { constant ->
        constant.name.equals(enumValueName, ignoreCase = true)
    } ?: throw IllegalArgumentException(
        "Enum value $enumValueName not found for type ${this.name}."
    )
}