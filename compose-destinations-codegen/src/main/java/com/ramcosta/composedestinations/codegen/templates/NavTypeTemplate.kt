package com.ramcosta.composedestinations.codegen.templates

import com.ramcosta.composedestinations.codegen.codeGenBasePackageName
import com.ramcosta.composedestinations.codegen.commons.CORE_PACKAGE_NAME

const val NAV_TYPE_NAME = "[NAV_TYPE_NAME]"
const val NAV_TYPE_CLASS_SIMPLE_NAME = "[NAV_TYPE_CLASS_SIMPLE_NAME]"
const val CLASS_SIMPLE_NAME_CAMEL_CASE = "[CLASS_SIMPLE_NAME_CAMEL_CASE]"
const val PARSE_VALUE_CAST_TO_CLASS = "[PARSE_VALUE_CAST_TO_CLASS]"
const val SERIALIZE_VALUE_CAST_TO_CLASS = "[SERIALIZE_VALUE_CAST_TO_CLASS]"
const val DESTINATIONS_NAV_TYPE_SERIALIZER_TYPE = "[DESTINATIONS_NAV_TYPE_SERIALIZER_TYPE]"
const val SERIALIZER_SIMPLE_CLASS_NAME = "[SERIALIZER_SIMPLE_CLASS_NAME]"

val parcelableNavTypeTemplate = """
package $codeGenBasePackageName.navtype

import android.os.Bundle
import android.os.Parcelable
import $CORE_PACKAGE_NAME.navargs.utils.encodeForRoute
import $CORE_PACKAGE_NAME.navargs.parcelable.DestinationsNavType
import $CORE_PACKAGE_NAME.navargs.parcelable.ParcelableNavTypeSerializer$ADDITIONAL_IMPORTS

val $NAV_TYPE_NAME = ${NAV_TYPE_CLASS_SIMPLE_NAME}($SERIALIZER_SIMPLE_CLASS_NAME)

class ${NAV_TYPE_CLASS_SIMPLE_NAME}(
    private val stringSerializer: ParcelableNavTypeSerializer<$DESTINATIONS_NAV_TYPE_SERIALIZER_TYPE>
) : DestinationsNavType<${CLASS_SIMPLE_NAME_CAMEL_CASE}?>() {

    override fun get(bundle: Bundle, key: String): ${CLASS_SIMPLE_NAME_CAMEL_CASE}? {
        return bundle.getParcelable(key)
    }

    override fun put(bundle: Bundle, key: String, value: ${CLASS_SIMPLE_NAME_CAMEL_CASE}?) {
        bundle.putParcelable(key, value)
    }

    override fun parseValue(value: String): $CLASS_SIMPLE_NAME_CAMEL_CASE {
        return stringSerializer.fromRouteString(value, ${CLASS_SIMPLE_NAME_CAMEL_CASE}::class.java)$PARSE_VALUE_CAST_TO_CLASS
    }

    override fun serializeValue(value: Parcelable): String {
        return encodeForRoute(stringSerializer.toRouteString(value$SERIALIZE_VALUE_CAST_TO_CLASS))
    }
}
""".trimIndent()

val serializableNavTypeTemplate = """
package $codeGenBasePackageName.navtype

import android.os.Bundle
import java.io.Serializable
import $CORE_PACKAGE_NAME.navargs.utils.encodeForRoute
import $CORE_PACKAGE_NAME.navargs.serializable.SerializableNavTypeSerializer
import $CORE_PACKAGE_NAME.navargs.serializable.SerializableDestinationsNavType$ADDITIONAL_IMPORTS

val $NAV_TYPE_NAME = ${NAV_TYPE_CLASS_SIMPLE_NAME}($SERIALIZER_SIMPLE_CLASS_NAME)

class ${NAV_TYPE_CLASS_SIMPLE_NAME}(
    private val stringSerializer: SerializableNavTypeSerializer<$DESTINATIONS_NAV_TYPE_SERIALIZER_TYPE>
) : SerializableDestinationsNavType<${CLASS_SIMPLE_NAME_CAMEL_CASE}?>() {

    override fun get(bundle: Bundle, key: String): ${CLASS_SIMPLE_NAME_CAMEL_CASE}? {
        return bundle.getSerializable(key) as ${CLASS_SIMPLE_NAME_CAMEL_CASE}?
    }

    override fun put(bundle: Bundle, key: String, value: ${CLASS_SIMPLE_NAME_CAMEL_CASE}?) {
        bundle.putSerializable(key, value)
    }

    override fun parseValue(value: String): $CLASS_SIMPLE_NAME_CAMEL_CASE {
        return stringSerializer.fromRouteString(value)$PARSE_VALUE_CAST_TO_CLASS
    }

    override fun serializeValue(value: Serializable): String {
        return encodeForRoute(stringSerializer.toRouteString(value$SERIALIZE_VALUE_CAST_TO_CLASS))
    }
}
""".trimIndent()

val customTypeSerializerNavTypeTemplate = """
package $codeGenBasePackageName.navtype

import android.os.Bundle
import $CORE_PACKAGE_NAME.navargs.utils.encodeForRoute
import $CORE_PACKAGE_NAME.navargs.custom.CustomTypeSerializer
import $CORE_PACKAGE_NAME.navargs.custom.CustomDestinationsNavType
$ADDITIONAL_IMPORTS

val $NAV_TYPE_NAME = CustomTypeSerializer${NAV_TYPE_CLASS_SIMPLE_NAME}($SERIALIZER_SIMPLE_CLASS_NAME)

class CustomTypeSerializer${NAV_TYPE_CLASS_SIMPLE_NAME}(
    private val customSerializer: CustomTypeSerializer<$DESTINATIONS_NAV_TYPE_SERIALIZER_TYPE>
) : CustomDestinationsNavType<${CLASS_SIMPLE_NAME_CAMEL_CASE}?>() {

    override fun get(bundle: Bundle, key: String): ${CLASS_SIMPLE_NAME_CAMEL_CASE}? =
        bundle.getString(key)?.let { parseValue(it) }

    override fun put(bundle: Bundle, key: String, value: ${CLASS_SIMPLE_NAME_CAMEL_CASE}?) {
        bundle.putString(key, serializeValue(value))
    }

    override fun parseValue(value: String): $CLASS_SIMPLE_NAME_CAMEL_CASE =
        customSerializer.fromRouteString(value)

    override fun serializeValue(value: ${CLASS_SIMPLE_NAME_CAMEL_CASE}?): String? =
        value?.let { encodeForRoute(customSerializer.toRouteString(it)) }
}
""".trimIndent()
