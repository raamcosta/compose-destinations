package com.ramcosta.composedestinations.codegen.templates

import com.ramcosta.composedestinations.codegen.commons.PACKAGE_NAME

const val NAV_TYPE_NAME = "[NAV_TYPE_NAME]"
const val CLASS_SIMPLE_NAME_CAMEL_CASE = "[CLASS_SIMPLE_NAME_CAMEL_CASE]"
const val PARSE_VALUE_CAST_TO_CLASS = "[PARSE_VALUE_CAST_TO_CLASS]"
const val SERIALIZE_VALUE_CAST_TO_CLASS = "[SERIALIZE_VALUE_CAST_TO_CLASS]"
const val DESTINATIONS_NAV_TYPE_SERIALIZER_TYPE = "[DESTINATIONS_NAV_TYPE_SERIALIZER_TYPE]"
const val SERIALIZER_SIMPLE_CLASS_NAME = "[SERIALIZER_SIMPLE_CLASS_NAME]"

val parcelableNavTypeTemplate = """
package $PACKAGE_NAME.navtype

import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import $PACKAGE_NAME.navargs.parcelable.DestinationsNavType
import $PACKAGE_NAME.navargs.parcelable.ParcelableNavTypeSerializer$ADDITIONAL_IMPORTS

val $NAV_TYPE_NAME = ${CLASS_SIMPLE_NAME_CAMEL_CASE}NavType($SERIALIZER_SIMPLE_CLASS_NAME)

class ${CLASS_SIMPLE_NAME_CAMEL_CASE}NavType(
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
        return Uri.encode(stringSerializer.toRouteString(value$SERIALIZE_VALUE_CAST_TO_CLASS))
    }
}
""".trimIndent()

val serializableNavTypeTemplate = """
package $PACKAGE_NAME.navtype

import android.net.Uri
import android.os.Bundle
import java.io.Serializable
import $PACKAGE_NAME.navargs.serializable.SerializableNavTypeSerializer
import $PACKAGE_NAME.navargs.serializable.SerializableDestinationsNavType$ADDITIONAL_IMPORTS

val $NAV_TYPE_NAME = ${CLASS_SIMPLE_NAME_CAMEL_CASE}NavType($SERIALIZER_SIMPLE_CLASS_NAME)

class ${CLASS_SIMPLE_NAME_CAMEL_CASE}NavType(
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
        return Uri.encode(stringSerializer.toRouteString(value$SERIALIZE_VALUE_CAST_TO_CLASS))
    }
}
""".trimIndent()