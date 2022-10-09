package com.ramcosta.composedestinations.codegen.templates.navtype.arrays

import com.ramcosta.composedestinations.codegen.codeGenBasePackageName
import com.ramcosta.composedestinations.codegen.commons.CORE_PACKAGE_NAME
import com.ramcosta.composedestinations.codegen.templates.core.FileTemplate
import com.ramcosta.composedestinations.codegen.templates.core.setOfImportable

val serializableArrayNavTypeTemplate = FileTemplate(
    packageStatement = "package $codeGenBasePackageName.navtype",
    imports = setOfImportable(
        "android.os.Bundle",
        "android.os.Parcelable",
        "java.io.Serializable",
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
@Suppress("UNCHECKED_CAST")
public class $ARRAY_CUSTOM_NAV_TYPE_NAME(
    private val serializer: DestinationsNavTypeSerializer<$SERIALIZER_TYPE_ARG_CLASS_SIMPLE_NAME>
) : DestinationsNavType<Array<$TYPE_ARG_CLASS_SIMPLE_NAME>?>() {


    override fun put(bundle: Bundle, key: String, value: Array<$TYPE_ARG_CLASS_SIMPLE_NAME>?) {
        bundle.putSerializable(key, value)
    }

    override fun get(bundle: Bundle, key: String): Array<$TYPE_ARG_CLASS_SIMPLE_NAME>? {
        return bundle.getSerializable(key) as Array<$TYPE_ARG_CLASS_SIMPLE_NAME>?
    }

    override fun parseValue(value: String): Array<$TYPE_ARG_CLASS_SIMPLE_NAME>? {
        return when (value) {
            DECODED_NULL -> null
            "[]" -> arrayOf()
            else -> {
                val splits = value.subSequence(1, value.length - 1).split(encodedComma)
                Array<$TYPE_ARG_CLASS_SIMPLE_NAME>(splits.size) {
                    serializer.fromRouteString(splits[it]) as $TYPE_ARG_CLASS_SIMPLE_NAME
                }
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

    override fun get(navBackStackEntry: NavBackStackEntry, key: String): Array<$TYPE_ARG_CLASS_SIMPLE_NAME>? {
        return navBackStackEntry.arguments?.getSerializable(key) as Array<$TYPE_ARG_CLASS_SIMPLE_NAME>?
    }

    override fun get(savedStateHandle: SavedStateHandle, key: String): Array<$TYPE_ARG_CLASS_SIMPLE_NAME>? {
        return savedStateHandle.get(key)
    }

}
""".trimIndent()
)