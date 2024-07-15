package com.ramcosta.composedestinations.codegen.templates.navtype.arrays

import com.ramcosta.composedestinations.codegen.codeGenBasePackageName
import com.ramcosta.composedestinations.codegen.commons.CORE_PACKAGE_NAME
import com.ramcosta.composedestinations.codegen.templates.core.FileTemplate
import com.ramcosta.composedestinations.codegen.templates.core.setOfImportable
import com.ramcosta.composedestinations.codegen.templates.navtype.NAV_TYPE_VISIBILITY

val customTypeArrayNavTypeTemplate = FileTemplate(
    packageStatement = "package $codeGenBasePackageName.navtype",
    imports = setOfImportable(
        "androidx.core.bundle.Bundle",
        "androidx.lifecycle.SavedStateHandle",
        "androidx.navigation.NavBackStackEntry",
        "$CORE_PACKAGE_NAME.navargs.DestinationsNavType",
        "$CORE_PACKAGE_NAME.navargs.DestinationsNavTypeSerializer",
        "$CORE_PACKAGE_NAME.navargs.primitives.DECODED_NULL",
        "$CORE_PACKAGE_NAME.navargs.primitives.ENCODED_NULL",
        "$CORE_PACKAGE_NAME.navargs.primitives.encodedComma",
        "$CORE_PACKAGE_NAME.navargs.utils.encodeForRoute",
    ),
    sourceCode = """
$NAV_TYPE_INITIALIZATION_CODE
$NAV_TYPE_VISIBILITY class $ARRAY_CUSTOM_NAV_TYPE_NAME(
    private val serializer: DestinationsNavTypeSerializer<$TYPE_ARG_CLASS_SIMPLE_NAME>
) : DestinationsNavType<Array<$TYPE_ARG_CLASS_SIMPLE_NAME>?>() {

    override fun put(bundle: Bundle, key: String, value: Array<$TYPE_ARG_CLASS_SIMPLE_NAME>?) {
        bundle.putStringArray(key, value?.toBundleArray())
    }

    override fun get(bundle: Bundle, key: String): Array<$TYPE_ARG_CLASS_SIMPLE_NAME>? {
        return bundle.getStringArray(key)?.toTypeArray()
    }

    override fun parseValue(value: String): Array<$TYPE_ARG_CLASS_SIMPLE_NAME>? {
        return when (value) {
            DECODED_NULL -> null
            "[]" -> arrayOf()
            else -> {
                value
                    .subSequence(1, value.length - 1)
                    .split(encodedComma)
                    .toTypedArray()
                    .toTypeArray()
            }
        }
    }

    override fun serializeValue(value: Array<$TYPE_ARG_CLASS_SIMPLE_NAME>?): String {
        return if (value == null) {
            ENCODED_NULL
        } else {
            encodeForRoute(
                "[" + value.joinToString(encodedComma) { serializer.toRouteString(it) } + "]"
            )
        }
    }

    override fun get(savedStateHandle: SavedStateHandle, key: String): Array<$TYPE_ARG_CLASS_SIMPLE_NAME>? {
        return savedStateHandle.get<Array<String>?>(key)?.toTypeArray()
    }
    
    override fun put(savedStateHandle: SavedStateHandle, key: String, value: Array<$TYPE_ARG_CLASS_SIMPLE_NAME>?) {
        savedStateHandle[key] = value?.toBundleArray()
    }

    private fun Array<$TYPE_ARG_CLASS_SIMPLE_NAME>.toBundleArray() =
        Array<String>(size) { serializer.toRouteString(this[it]) }

    private fun Array<String>.toTypeArray() =
        Array<$TYPE_ARG_CLASS_SIMPLE_NAME>(size) { serializer.fromRouteString(this[it]) }
}
""".trimIndent()
)