package com.ramcosta.composedestinations.codegen.templates.navtype

import com.ramcosta.composedestinations.codegen.codeGenBasePackageName
import com.ramcosta.composedestinations.codegen.commons.CORE_PACKAGE_NAME
import com.ramcosta.composedestinations.codegen.templates.core.FileTemplate
import com.ramcosta.composedestinations.codegen.templates.core.setOfImportable

val customTypeSerializerNavTypeTemplate = FileTemplate(
    packageStatement = "package $codeGenBasePackageName.navtype",
    imports = setOfImportable(
        "android.os.Bundle",
        "androidx.lifecycle.SavedStateHandle",
        "$CORE_PACKAGE_NAME.navargs.DestinationsNavType",
        "$CORE_PACKAGE_NAME.navargs.DestinationsNavTypeSerializer",
        "$CORE_PACKAGE_NAME.navargs.utils.encodeForRoute",
        "$CORE_PACKAGE_NAME.navargs.primitives.DECODED_NULL",
        "$CORE_PACKAGE_NAME.navargs.primitives.ENCODED_NULL",
    ),
    sourceCode = """
$NAV_TYPE_VISIBILITY val $NAV_TYPE_NAME: $NAV_TYPE_CLASS_SIMPLE_NAME = $NAV_TYPE_CLASS_SIMPLE_NAME($SERIALIZER_SIMPLE_CLASS_NAME)

$NAV_TYPE_VISIBILITY class $NAV_TYPE_CLASS_SIMPLE_NAME(
    private val customSerializer: DestinationsNavTypeSerializer<$DESTINATIONS_NAV_TYPE_SERIALIZER_TYPE>
) : DestinationsNavType<$CLASS_SIMPLE_NAME_CAMEL_CASE?>() {

    override fun get(bundle: Bundle, key: String): $CLASS_SIMPLE_NAME_CAMEL_CASE? =
        bundle.getString(key)?.let { parseValue(it) }

    override fun put(bundle: Bundle, key: String, value: $CLASS_SIMPLE_NAME_CAMEL_CASE?) {
        bundle.putString(key, value?.let { customSerializer.toRouteString(it) })
    }

    override fun parseValue(value: String): $CLASS_SIMPLE_NAME_CAMEL_CASE? {
        return if (value == DECODED_NULL) {
            null
        } else {
            customSerializer.fromRouteString(value)       
        }
    }

    override fun serializeValue(value: $CLASS_SIMPLE_NAME_CAMEL_CASE?): String {
        return if (value == null) {
            ENCODED_NULL
        } else {
            encodeForRoute(customSerializer.toRouteString(value))
        }
    }

    override fun get(savedStateHandle: SavedStateHandle, key: String): $CLASS_SIMPLE_NAME_CAMEL_CASE? {
        return savedStateHandle.get<String>(key)?.let { customSerializer.fromRouteString(it) }
    }

    override fun put(savedStateHandle: SavedStateHandle, key: String, value: $CLASS_SIMPLE_NAME_CAMEL_CASE?) {
        savedStateHandle[key] = value?.let { customSerializer.toRouteString(it) }
    }
}
""".trimIndent()
)