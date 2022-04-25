package com.ramcosta.composedestinations.codegen.templates

import com.ramcosta.composedestinations.codegen.codeGenBasePackageName
import com.ramcosta.composedestinations.codegen.commons.CORE_PACKAGE_NAME

const val TYPE_ARG_CLASS_SIMPLE_NAME = "[TYPE_ARG_CLASS_SIMPLE_NAME]"
const val ARRAY_CUSTOM_NAV_TYPE_NAME = "[ARRAY_CUSTOM_NAV_TYPE_NAME]"
const val SERIALIZER_TYPE_ARG_CLASS_SIMPLE_NAME = "[SERIALIZER_TYPE_ARG_CLASS_SIMPLE_NAME]"
const val NAV_TYPE_INITIALIZATION_CODE = "[NAV_TYPE_INITIALIZATION_CODE]"

val ktxSerializableArrayNavTypeTemplate = """
package $codeGenBasePackageName.navtype

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavBackStackEntry
import $CORE_PACKAGE_NAME.navargs.DestinationsNavType
import $CORE_PACKAGE_NAME.navargs.DestinationsNavTypeSerializer
import $CORE_PACKAGE_NAME.navargs.primitives.DECODED_NULL
import $CORE_PACKAGE_NAME.navargs.primitives.ENCODED_NULL
import $CORE_PACKAGE_NAME.navargs.primitives.encodedComma
import $CORE_PACKAGE_NAME.navargs.utils.encodeForRoute
import kotlinx.serialization.ExperimentalSerializationApi
$ADDITIONAL_IMPORTS

@OptIn(ExperimentalSerializationApi::class)
$NAV_TYPE_INITIALIZATION_CODE
@OptIn(ExperimentalSerializationApi::class)
class $ARRAY_CUSTOM_NAV_TYPE_NAME(
    private val serializer: DefaultKtxSerializableNavTypeSerializer<$TYPE_ARG_CLASS_SIMPLE_NAME>
) : DestinationsNavType<Array<$TYPE_ARG_CLASS_SIMPLE_NAME>?>() {

    override fun put(bundle: Bundle, key: String, value: Array<$TYPE_ARG_CLASS_SIMPLE_NAME>?) {
        bundle.putParcelableArray(key, value?.toBundleArray())
    }

    override fun get(bundle: Bundle, key: String): Array<$TYPE_ARG_CLASS_SIMPLE_NAME>? {
        return bundle.getParcelableArray(key)?.toTypeArray()
    }

    override fun parseValue(value: String): Array<$TYPE_ARG_CLASS_SIMPLE_NAME>? {
        return if (value == DECODED_NULL) {
            null
        } else {
            return value.split(encodedComma).map {
                serializer.fromRouteString(it)
            }.toTypedArray()
        }
    }

    override fun serializeValue(value: Array<$TYPE_ARG_CLASS_SIMPLE_NAME>?): String {
        return if (value == null) {
            ENCODED_NULL
        } else {
            encodeForRoute(
                value.joinToString(encodedComma) { serializer.toRouteString(it) }
            )
        }
    }

    override fun get(navBackStackEntry: NavBackStackEntry, key: String): Array<$TYPE_ARG_CLASS_SIMPLE_NAME>? {
        return navBackStackEntry.arguments?.getParcelableArray(key)?.toTypeArray()
    }

    override fun get(savedStateHandle: SavedStateHandle, key: String): Array<$TYPE_ARG_CLASS_SIMPLE_NAME>? {
        return savedStateHandle.get<Array<Parcelable>?>(key)?.toTypeArray()
    }

    private fun Array<$TYPE_ARG_CLASS_SIMPLE_NAME>.toBundleArray() =
        Array<ParcelableByteArray>(size) { ParcelableByteArray(serializer.toByteArray(this[it])) }

    private fun Array<Parcelable>.toTypeArray() =
        Array<$TYPE_ARG_CLASS_SIMPLE_NAME>(size) { serializer.fromByteArray((this[it] as ParcelableByteArray).value) }

    private class ParcelableByteArray(
        val value: ByteArray
    ): Parcelable {

        constructor(parcel: Parcel) : this(parcel.createByteArray()!!)

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeByteArray(value)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<ParcelableByteArray> {
            override fun createFromParcel(parcel: Parcel): ParcelableByteArray {
                return ParcelableByteArray(parcel)
            }

            override fun newArray(size: Int): Array<ParcelableByteArray?> {
                return arrayOfNulls(size)
            }
        }
    }
}
""".trimIndent()

val customTypeArrayNavTypeTemplate = """
package $codeGenBasePackageName.navtype

import android.os.Bundle
import android.os.Parcelable
import java.io.Serializable
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavBackStackEntry
import $CORE_PACKAGE_NAME.navargs.DestinationsNavType
import $CORE_PACKAGE_NAME.navargs.DestinationsNavTypeSerializer
import $CORE_PACKAGE_NAME.navargs.primitives.DECODED_NULL
import $CORE_PACKAGE_NAME.navargs.primitives.ENCODED_NULL
import $CORE_PACKAGE_NAME.navargs.primitives.encodedComma
import $CORE_PACKAGE_NAME.navargs.utils.encodeForRoute
$ADDITIONAL_IMPORTS

$NAV_TYPE_INITIALIZATION_CODE
class $ARRAY_CUSTOM_NAV_TYPE_NAME(
    private val serializer: DestinationsNavTypeSerializer<$TYPE_ARG_CLASS_SIMPLE_NAME>
) : DestinationsNavType<Array<$TYPE_ARG_CLASS_SIMPLE_NAME>?>() {

    override fun put(bundle: Bundle, key: String, value: Array<$TYPE_ARG_CLASS_SIMPLE_NAME>?) {
        bundle.putStringArray(key, value?.toBundleArray())
    }

    override fun get(bundle: Bundle, key: String): Array<$TYPE_ARG_CLASS_SIMPLE_NAME>? {
        return bundle.getStringArray(key)?.toTypeArray()
    }

    override fun parseValue(value: String): Array<$TYPE_ARG_CLASS_SIMPLE_NAME>? {
        return if (value == DECODED_NULL) {
            null
        } else {
            val splits = value.split(encodedComma).toTypedArray()
            return splits.toTypeArray()
        }
    }

    override fun serializeValue(value: Array<$TYPE_ARG_CLASS_SIMPLE_NAME>?): String {
        return if (value == null) {
            ENCODED_NULL
        } else {
            encodeForRoute(
                value.joinToString(encodedComma) { serializer.toRouteString(it) }
            )
        }
    }

    override fun get(navBackStackEntry: NavBackStackEntry, key: String): Array<$TYPE_ARG_CLASS_SIMPLE_NAME>? {
        return navBackStackEntry.arguments?.getStringArray(key)?.toTypeArray()
    }

    override fun get(savedStateHandle: SavedStateHandle, key: String): Array<$TYPE_ARG_CLASS_SIMPLE_NAME>? {
        return savedStateHandle.get<Array<String>?>(key)?.toTypeArray()
    }

    private fun Array<$TYPE_ARG_CLASS_SIMPLE_NAME>.toBundleArray() =
        Array<String>(size) { serializer.toRouteString(this[it]) }

    private fun Array<String>.toTypeArray() =
        Array<$TYPE_ARG_CLASS_SIMPLE_NAME>(size) { serializer.fromRouteString(this[it]) }
}
""".trimIndent()

val parcelableArrayNavTypeTemplate = """
package $codeGenBasePackageName.navtype

import android.os.Bundle
import android.os.Parcelable
import java.io.Serializable
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavBackStackEntry
import $CORE_PACKAGE_NAME.navargs.DestinationsNavType
import $CORE_PACKAGE_NAME.navargs.DestinationsNavTypeSerializer
import $CORE_PACKAGE_NAME.navargs.primitives.DECODED_NULL
import $CORE_PACKAGE_NAME.navargs.primitives.ENCODED_NULL
import $CORE_PACKAGE_NAME.navargs.primitives.encodedComma
import $CORE_PACKAGE_NAME.navargs.utils.encodeForRoute$ADDITIONAL_IMPORTS

$NAV_TYPE_INITIALIZATION_CODE
@Suppress("UNCHECKED_CAST")
class $ARRAY_CUSTOM_NAV_TYPE_NAME(
    private val serializer: DestinationsNavTypeSerializer<$SERIALIZER_TYPE_ARG_CLASS_SIMPLE_NAME>
) : DestinationsNavType<Array<$TYPE_ARG_CLASS_SIMPLE_NAME>?>() {


    override fun put(bundle: Bundle, key: String, value: Array<$TYPE_ARG_CLASS_SIMPLE_NAME>?) {
        bundle.putParcelableArray(key, value)
    }

    override fun get(bundle: Bundle, key: String): Array<$TYPE_ARG_CLASS_SIMPLE_NAME>? {
        return bundle.getParcelableArray(key) as Array<$TYPE_ARG_CLASS_SIMPLE_NAME>?
    }

    override fun parseValue(value: String): Array<$TYPE_ARG_CLASS_SIMPLE_NAME>? {
        return if (value == DECODED_NULL) {
            null
        } else {
            val splits = value.split(encodedComma)
            return Array<$TYPE_ARG_CLASS_SIMPLE_NAME>(splits.size) {
                serializer.fromRouteString(splits[it]) as $TYPE_ARG_CLASS_SIMPLE_NAME
            }
        }
    }

    override fun serializeValue(value: Array<$TYPE_ARG_CLASS_SIMPLE_NAME>?): String {
        return if (value == null) {
            ENCODED_NULL
        } else {
            encodeForRoute(
                value.joinToString(encodedComma) { serializer.toRouteString(it) }
            )
        }
    }

    override fun get(navBackStackEntry: NavBackStackEntry, key: String): Array<$TYPE_ARG_CLASS_SIMPLE_NAME>? {
        return navBackStackEntry.arguments?.getParcelableArray(key) as Array<$TYPE_ARG_CLASS_SIMPLE_NAME>?
    }

    override fun get(savedStateHandle: SavedStateHandle, key: String): Array<$TYPE_ARG_CLASS_SIMPLE_NAME>? {
        return savedStateHandle.get(key)
    }

}
""".trimIndent()

val serializableArrayNavTypeTemplate = """
package $codeGenBasePackageName.navtype

import android.os.Bundle
import android.os.Parcelable
import java.io.Serializable
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavBackStackEntry
import $CORE_PACKAGE_NAME.navargs.DestinationsNavType
import $CORE_PACKAGE_NAME.navargs.DestinationsNavTypeSerializer
import $CORE_PACKAGE_NAME.navargs.primitives.DECODED_NULL
import $CORE_PACKAGE_NAME.navargs.primitives.ENCODED_NULL
import $CORE_PACKAGE_NAME.navargs.primitives.encodedComma
import $CORE_PACKAGE_NAME.navargs.utils.encodeForRoute$ADDITIONAL_IMPORTS

$NAV_TYPE_INITIALIZATION_CODE
@Suppress("UNCHECKED_CAST")
class $ARRAY_CUSTOM_NAV_TYPE_NAME(
    private val serializer: DestinationsNavTypeSerializer<$SERIALIZER_TYPE_ARG_CLASS_SIMPLE_NAME>
) : DestinationsNavType<Array<$TYPE_ARG_CLASS_SIMPLE_NAME>?>() {


    override fun put(bundle: Bundle, key: String, value: Array<$TYPE_ARG_CLASS_SIMPLE_NAME>?) {
        bundle.putSerializable(key, value)
    }

    override fun get(bundle: Bundle, key: String): Array<$TYPE_ARG_CLASS_SIMPLE_NAME>? {
        return bundle.getSerializable(key) as Array<$TYPE_ARG_CLASS_SIMPLE_NAME>?
    }

    override fun parseValue(value: String): Array<$TYPE_ARG_CLASS_SIMPLE_NAME>? {
        return if (value == DECODED_NULL) {
            null
        } else {
            val splits = value.split(encodedComma)
            return Array<$TYPE_ARG_CLASS_SIMPLE_NAME>(splits.size) {
                serializer.fromRouteString(splits[it]) as $TYPE_ARG_CLASS_SIMPLE_NAME
            }
        }
    }

    override fun serializeValue(value: Array<$TYPE_ARG_CLASS_SIMPLE_NAME>?): String {
        return if (value == null) {
            ENCODED_NULL
        } else {
            encodeForRoute(
                value.joinToString(encodedComma) { serializer.toRouteString(it) }
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
