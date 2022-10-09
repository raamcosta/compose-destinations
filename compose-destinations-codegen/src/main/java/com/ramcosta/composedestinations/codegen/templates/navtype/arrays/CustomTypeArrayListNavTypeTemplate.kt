package com.ramcosta.composedestinations.codegen.templates.navtype.arrays

import com.ramcosta.composedestinations.codegen.codeGenBasePackageName
import com.ramcosta.composedestinations.codegen.commons.CORE_PACKAGE_NAME
import com.ramcosta.composedestinations.codegen.templates.core.FileTemplate
import com.ramcosta.composedestinations.codegen.templates.core.setOfImportable

val customTypeArrayListNavTypeTemplate = FileTemplate(
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
public class $ARRAY_CUSTOM_NAV_TYPE_NAME(
    private val serializer: DestinationsNavTypeSerializer<$SERIALIZER_TYPE_ARG_CLASS_SIMPLE_NAME>
) : DestinationsNavType<ArrayList<$TYPE_ARG_CLASS_SIMPLE_NAME>?>() {

    override fun put(bundle: Bundle, key: String, value: ArrayList<$TYPE_ARG_CLASS_SIMPLE_NAME>?) {
        bundle.putStringArrayList(key, value?.toBundleArrayList())
    }

    override fun get(bundle: Bundle, key: String): ArrayList<$TYPE_ARG_CLASS_SIMPLE_NAME>? {
        return bundle.getStringArrayList(key)?.toTypeArrayList()
    }

    override fun parseValue(value: String): ArrayList<$TYPE_ARG_CLASS_SIMPLE_NAME>? {
        return when (value) {
            DECODED_NULL -> null
            "[]" -> arrayListOf()
            else -> {
                val contentValue = value.subSequence(1, value.length - 1)
                val splits = contentValue.split(encodedComma)
                splits.toTypeArrayList()
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

    override fun get(navBackStackEntry: NavBackStackEntry, key: String): ArrayList<$TYPE_ARG_CLASS_SIMPLE_NAME>? {
        return navBackStackEntry.arguments?.getStringArrayList(key)?.toTypeArrayList()
    }

    override fun get(savedStateHandle: SavedStateHandle, key: String): ArrayList<$TYPE_ARG_CLASS_SIMPLE_NAME>? {
        return savedStateHandle.get<ArrayList<String>?>(key)?.toTypeArrayList()
    }

    private fun List<$TYPE_ARG_CLASS_SIMPLE_NAME>.toBundleArrayList() =
        mapTo(ArrayList()) { serializer.toRouteString(it) }

    private fun List<String>.toTypeArrayList() =
        mapTo(ArrayList()) { serializer.fromRouteString(it) }
}
""".trimIndent()
)