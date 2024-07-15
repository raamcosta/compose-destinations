package com.ramcosta.composedestinations.codegen.templates.navtype.arrays

import com.ramcosta.composedestinations.codegen.codeGenBasePackageName
import com.ramcosta.composedestinations.codegen.commons.CORE_PACKAGE_NAME
import com.ramcosta.composedestinations.codegen.templates.core.FileTemplate
import com.ramcosta.composedestinations.codegen.templates.core.setOfImportable
import com.ramcosta.composedestinations.codegen.templates.navtype.NAV_TYPE_VISIBILITY

val serializableArrayListNavTypeTemplate = FileTemplate(
    packageStatement = "package $codeGenBasePackageName.navtype",
    imports = setOfImportable(
        "androidx.core.bundle.Bundle",

        "androidx.lifecycle.SavedStateHandle",
        "$CORE_PACKAGE_NAME.navargs.DestinationsNavType",
        "$CORE_PACKAGE_NAME.navargs.DestinationsNavTypeSerializer",
        "$CORE_PACKAGE_NAME.navargs.primitives.DECODED_NULL",
        "$CORE_PACKAGE_NAME.navargs.primitives.ENCODED_NULL",
        "$CORE_PACKAGE_NAME.navargs.primitives.encodedComma",
        "$CORE_PACKAGE_NAME.navargs.utils.encodeForRoute",
    ),
    sourceCode = """
$NAV_TYPE_INITIALIZATION_CODE
@Suppress("UNCHECKED_CAST")
$NAV_TYPE_VISIBILITY class $ARRAY_CUSTOM_NAV_TYPE_NAME(
    private val serializer: DestinationsNavTypeSerializer<$SERIALIZER_TYPE_ARG_CLASS_SIMPLE_NAME>
) : DestinationsNavType<ArrayList<$TYPE_ARG_CLASS_SIMPLE_NAME>?>() {


    override fun put(bundle: Bundle, key: String, value: ArrayList<$TYPE_ARG_CLASS_SIMPLE_NAME>?) {
        bundle.putSerializable(key, value)
    }

    override fun get(bundle: Bundle, key: String): ArrayList<$TYPE_ARG_CLASS_SIMPLE_NAME>? {
        @Suppress("DEPRECATION")
        return bundle.getSerializable(key) as ArrayList<$TYPE_ARG_CLASS_SIMPLE_NAME>?
    }

    override fun parseValue(value: String): ArrayList<$TYPE_ARG_CLASS_SIMPLE_NAME>? {
        return when (value) {
            DECODED_NULL -> null
            "[]" -> arrayListOf()
            else -> {
                val contentValue = value.subSequence(1, value.length - 1)
                val splits = contentValue.split(encodedComma)
                splits.mapTo(ArrayList()) {
                    serializer.fromRouteString(it) as $TYPE_ARG_CLASS_SIMPLE_NAME
                }
            }
        }
    }

    override fun serializeValue(value: ArrayList<$TYPE_ARG_CLASS_SIMPLE_NAME>?): String {
        return if (value == null) {
            ENCODED_NULL
        } else {
            encodeForRoute(
                "[" + value.joinToString(encodedComma) { serializer.toRouteString(it) } + "]"
            )
        }
    }

    override fun get(savedStateHandle: SavedStateHandle, key: String): ArrayList<$TYPE_ARG_CLASS_SIMPLE_NAME>? {
        return savedStateHandle.get(key)
    }
    
    override fun put(savedStateHandle: SavedStateHandle, key: String, value: ArrayList<$TYPE_ARG_CLASS_SIMPLE_NAME>?) {
        savedStateHandle[key] = value
    }

}
""".trimIndent()
)
