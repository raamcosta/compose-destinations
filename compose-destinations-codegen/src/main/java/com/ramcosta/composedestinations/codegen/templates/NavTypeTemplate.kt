package com.ramcosta.composedestinations.codegen.templates

import com.ramcosta.composedestinations.codegen.codeGenBasePackageName
import com.ramcosta.composedestinations.codegen.commons.CORE_PACKAGE_NAME

const val NAV_TYPE_NAME = "[NAV_TYPE_NAME]"
const val NAV_TYPE_CLASS_SIMPLE_NAME = "[NAV_TYPE_CLASS_SIMPLE_NAME]"
const val CLASS_SIMPLE_NAME_CAMEL_CASE = "[CLASS_SIMPLE_NAME_CAMEL_CASE]"
const val PARSE_VALUE_CAST_TO_CLASS = "[PARSE_VALUE_CAST_TO_CLASS]"
const val DESTINATIONS_NAV_TYPE_SERIALIZER_TYPE = "[DESTINATIONS_NAV_TYPE_SERIALIZER_TYPE]"
const val SERIALIZER_SIMPLE_CLASS_NAME = "[SERIALIZER_SIMPLE_CLASS_NAME]"

val parcelableNavTypeTemplate = """
package $codeGenBasePackageName.navtype

import android.os.Bundle
import android.os.Parcelable
import $CORE_PACKAGE_NAME.navargs.DestinationsNavType
import $CORE_PACKAGE_NAME.navargs.DestinationsNavTypeSerializer
import $CORE_PACKAGE_NAME.navargs.utils.encodeForRoute$ADDITIONAL_IMPORTS

val $NAV_TYPE_NAME = ${NAV_TYPE_CLASS_SIMPLE_NAME}($SERIALIZER_SIMPLE_CLASS_NAME)

class ${NAV_TYPE_CLASS_SIMPLE_NAME}(
    private val stringSerializer: DestinationsNavTypeSerializer<$DESTINATIONS_NAV_TYPE_SERIALIZER_TYPE>
) : DestinationsNavType<${CLASS_SIMPLE_NAME_CAMEL_CASE}?>() {

    override fun get(bundle: Bundle, key: String): ${CLASS_SIMPLE_NAME_CAMEL_CASE}? {
        return bundle.getParcelable(key)
    }

    override fun put(bundle: Bundle, key: String, value: ${CLASS_SIMPLE_NAME_CAMEL_CASE}?) {
        bundle.putParcelable(key, value)
    }

    override fun parseValue(value: String): $CLASS_SIMPLE_NAME_CAMEL_CASE {
        return stringSerializer.fromRouteString(value)$PARSE_VALUE_CAST_TO_CLASS
    }

    override fun serializeValue(value: $CLASS_SIMPLE_NAME_CAMEL_CASE?): String? =
        value?.let { encodeForRoute(stringSerializer.toRouteString(value)) }
}
""".trimIndent()

val serializableNavTypeTemplate = """
package $codeGenBasePackageName.navtype

import android.os.Bundle
import java.io.Serializable
import $CORE_PACKAGE_NAME.navargs.DestinationsNavType
import $CORE_PACKAGE_NAME.navargs.DestinationsNavTypeSerializer
import $CORE_PACKAGE_NAME.navargs.utils.encodeForRoute$ADDITIONAL_IMPORTS

val $NAV_TYPE_NAME = ${NAV_TYPE_CLASS_SIMPLE_NAME}($SERIALIZER_SIMPLE_CLASS_NAME)

class ${NAV_TYPE_CLASS_SIMPLE_NAME}(
    private val stringSerializer: DestinationsNavTypeSerializer<$DESTINATIONS_NAV_TYPE_SERIALIZER_TYPE>
) : DestinationsNavType<${CLASS_SIMPLE_NAME_CAMEL_CASE}?>() {

    override fun get(bundle: Bundle, key: String): ${CLASS_SIMPLE_NAME_CAMEL_CASE}? {
        return bundle.getSerializable(key) as ${CLASS_SIMPLE_NAME_CAMEL_CASE}?
    }

    override fun put(bundle: Bundle, key: String, value: ${CLASS_SIMPLE_NAME_CAMEL_CASE}?) {
        bundle.putSerializable(key, value)
    }

    override fun parseValue(value: String): $CLASS_SIMPLE_NAME_CAMEL_CASE {
        return stringSerializer.fromRouteString(value)
    }

    override fun serializeValue(value: ${CLASS_SIMPLE_NAME_CAMEL_CASE}?): String? =
        value?.let { encodeForRoute(stringSerializer.toRouteString(value)) }
}
""".trimIndent()

val customTypeSerializerNavTypeTemplate = """
package $codeGenBasePackageName.navtype

import android.os.Bundle
import $CORE_PACKAGE_NAME.navargs.DestinationsNavType
import $CORE_PACKAGE_NAME.navargs.DestinationsNavTypeSerializer
import $CORE_PACKAGE_NAME.navargs.utils.encodeForRoute
$ADDITIONAL_IMPORTS

val $NAV_TYPE_NAME = ${NAV_TYPE_CLASS_SIMPLE_NAME}($SERIALIZER_SIMPLE_CLASS_NAME)

class ${NAV_TYPE_CLASS_SIMPLE_NAME}(
    private val customSerializer: DestinationsNavTypeSerializer<$DESTINATIONS_NAV_TYPE_SERIALIZER_TYPE>
) : DestinationsNavType<${CLASS_SIMPLE_NAME_CAMEL_CASE}?>() {

    override fun get(bundle: Bundle, key: String): ${CLASS_SIMPLE_NAME_CAMEL_CASE}? =
        bundle.getString(key)?.let { parseValue(it) }

    override fun put(bundle: Bundle, key: String, value: ${CLASS_SIMPLE_NAME_CAMEL_CASE}?) {
        bundle.putString(key, serializeValue(value))
    }

    override fun parseValue(value: String): $CLASS_SIMPLE_NAME_CAMEL_CASE =
        customSerializer.fromRouteString(value)

    override fun serializeValue(value: ${CLASS_SIMPLE_NAME_CAMEL_CASE}?): String? =
        value?.let { encodeForRoute(customSerializer.toRouteString(value)) }
}
""".trimIndent()
