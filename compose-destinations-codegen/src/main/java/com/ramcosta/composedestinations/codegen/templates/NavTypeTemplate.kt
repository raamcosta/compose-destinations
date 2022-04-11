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
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavBackStackEntry
import $CORE_PACKAGE_NAME.navargs.DestinationsNavType
import $CORE_PACKAGE_NAME.navargs.DestinationsNavTypeSerializer
import $CORE_PACKAGE_NAME.navargs.utils.encodeForRoute
import $CORE_PACKAGE_NAME.navargs.primitives.DECODED_NULL
import $CORE_PACKAGE_NAME.navargs.primitives.ENCODED_NULL$ADDITIONAL_IMPORTS

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

    override fun parseValue(value: String): $CLASS_SIMPLE_NAME_CAMEL_CASE? {
        return if (value == DECODED_NULL) {
            null
        } else {
            stringSerializer.fromRouteString(value)$PARSE_VALUE_CAST_TO_CLASS       
        }
    }

    override fun serializeValue(value: $CLASS_SIMPLE_NAME_CAMEL_CASE?): String {
        return if (value == null) {
            ENCODED_NULL
        } else {
            encodeForRoute(stringSerializer.toRouteString(value))
        }
    }
    
    fun get(navBackStackEntry: NavBackStackEntry, key: String): $CLASS_SIMPLE_NAME_CAMEL_CASE? {
        return navBackStackEntry.arguments?.getParcelable(key)
    }

    fun get(savedStateHandle: SavedStateHandle, key: String): $CLASS_SIMPLE_NAME_CAMEL_CASE? {
        return savedStateHandle.get(key)
    }
}
""".trimIndent()

val serializableNavTypeTemplate = """
package $codeGenBasePackageName.navtype

import android.os.Bundle
import java.io.Serializable
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavBackStackEntry
import $CORE_PACKAGE_NAME.navargs.DestinationsNavType
import $CORE_PACKAGE_NAME.navargs.DestinationsNavTypeSerializer
import $CORE_PACKAGE_NAME.navargs.utils.encodeForRoute
import $CORE_PACKAGE_NAME.navargs.primitives.DECODED_NULL
import $CORE_PACKAGE_NAME.navargs.primitives.ENCODED_NULL$ADDITIONAL_IMPORTS

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

    override fun parseValue(value: String): $CLASS_SIMPLE_NAME_CAMEL_CASE? {
        return if (value == DECODED_NULL) {
            null
        } else {
            stringSerializer.fromRouteString(value)$PARSE_VALUE_CAST_TO_CLASS       
        }
    }

    override fun serializeValue(value: $CLASS_SIMPLE_NAME_CAMEL_CASE?): String {
        return if (value == null) {
            ENCODED_NULL
        } else {
            encodeForRoute(stringSerializer.toRouteString(value))
        }
    }

    fun get(navBackStackEntry: NavBackStackEntry, key: String): $CLASS_SIMPLE_NAME_CAMEL_CASE? {
        return navBackStackEntry.arguments?.getSerializable(key) as? $CLASS_SIMPLE_NAME_CAMEL_CASE?
    }

    fun get(savedStateHandle: SavedStateHandle, key: String): $CLASS_SIMPLE_NAME_CAMEL_CASE? {
        return savedStateHandle.get(key)
    }
}
""".trimIndent()

val customTypeSerializerNavTypeTemplate = """
package $codeGenBasePackageName.navtype

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavBackStackEntry
import $CORE_PACKAGE_NAME.navargs.DestinationsNavType
import $CORE_PACKAGE_NAME.navargs.DestinationsNavTypeSerializer
import $CORE_PACKAGE_NAME.navargs.utils.encodeForRoute
import $CORE_PACKAGE_NAME.navargs.primitives.DECODED_NULL
import $CORE_PACKAGE_NAME.navargs.primitives.ENCODED_NULL
$ADDITIONAL_IMPORTS

val $NAV_TYPE_NAME = ${NAV_TYPE_CLASS_SIMPLE_NAME}($SERIALIZER_SIMPLE_CLASS_NAME)

class ${NAV_TYPE_CLASS_SIMPLE_NAME}(
    private val customSerializer: DestinationsNavTypeSerializer<$DESTINATIONS_NAV_TYPE_SERIALIZER_TYPE>
) : DestinationsNavType<${CLASS_SIMPLE_NAME_CAMEL_CASE}?>() {

    override fun get(bundle: Bundle, key: String): ${CLASS_SIMPLE_NAME_CAMEL_CASE}? =
        bundle.getString(key)?.let { parseValue(it) }

    override fun put(bundle: Bundle, key: String, value: ${CLASS_SIMPLE_NAME_CAMEL_CASE}?) {
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

    fun get(navBackStackEntry: NavBackStackEntry, key: String): $CLASS_SIMPLE_NAME_CAMEL_CASE? {
        return navBackStackEntry.arguments?.getString(key)?.let { customSerializer.fromRouteString(it) }
    }

    fun get(savedStateHandle: SavedStateHandle, key: String): $CLASS_SIMPLE_NAME_CAMEL_CASE? {
        return savedStateHandle.get<String>(key)?.let { customSerializer.fromRouteString(it) }
    }
}
""".trimIndent()

val ktxSerializableNavTypeTemplate = """
package $codeGenBasePackageName.navtype

import android.os.Bundle
import java.io.Serializable
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavBackStackEntry
import $CORE_PACKAGE_NAME.navargs.DestinationsNavType
import $CORE_PACKAGE_NAME.navargs.utils.encodeForRoute
import $CORE_PACKAGE_NAME.navargs.primitives.DECODED_NULL
import $CORE_PACKAGE_NAME.navargs.primitives.ENCODED_NULL
import kotlinx.serialization.ExperimentalSerializationApi
$ADDITIONAL_IMPORTS

@OptIn(ExperimentalSerializationApi::class)
val $NAV_TYPE_NAME = ${NAV_TYPE_CLASS_SIMPLE_NAME}(
    $SERIALIZER_SIMPLE_CLASS_NAME
)

@OptIn(ExperimentalSerializationApi::class)
class ${NAV_TYPE_CLASS_SIMPLE_NAME}(
    private val serializer: DefaultKtxSerializableNavTypeSerializer<$CLASS_SIMPLE_NAME_CAMEL_CASE>
) : DestinationsNavType<${CLASS_SIMPLE_NAME_CAMEL_CASE}?>() {

    override fun get(bundle: Bundle, key: String): ${CLASS_SIMPLE_NAME_CAMEL_CASE}? =
        bundle.getByteArray(key)?.let { fromByteArray(it) }

    override fun put(bundle: Bundle, key: String, value: ${CLASS_SIMPLE_NAME_CAMEL_CASE}?) {
        bundle.putByteArray(key, value?.let { toByteArray(it) })
    }

    override fun parseValue(value: String): $CLASS_SIMPLE_NAME_CAMEL_CASE? {
        return if (value == DECODED_NULL) {
            null
        } else {
            serializer.fromRouteString(value)       
        }
    }

    override fun serializeValue(value: $CLASS_SIMPLE_NAME_CAMEL_CASE?): String {
        return if (value == null) {
            ENCODED_NULL
        } else {
            encodeForRoute(serializer.toRouteString(value))
        }
    }

    fun get(navBackStackEntry: NavBackStackEntry, key: String): $CLASS_SIMPLE_NAME_CAMEL_CASE? {
        return navBackStackEntry.arguments?.getByteArray(key)?.let { fromByteArray(it) }
    }

    fun get(savedStateHandle: SavedStateHandle, key: String): $CLASS_SIMPLE_NAME_CAMEL_CASE? {
        return savedStateHandle.get<ByteArray>(key)?.let { fromByteArray(it) }
    }

    private fun fromByteArray(bytes: ByteArray): $CLASS_SIMPLE_NAME_CAMEL_CASE = serializer.fromByteArray(bytes)

    private fun toByteArray(value: $CLASS_SIMPLE_NAME_CAMEL_CASE): ByteArray = serializer.toByteArray(value)

}
""".trimIndent()
