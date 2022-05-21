package com.ramcosta.composedestinations.codegen.templates

import com.ramcosta.composedestinations.codegen.codeGenBasePackageName
import com.ramcosta.composedestinations.codegen.commons.CORE_PACKAGE_NAME

val ktxSerializableArrayListNavTypeTemplate = """
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
    private val serializer: DefaultKtxSerializableNavTypeSerializer<$SERIALIZER_TYPE_ARG_CLASS_SIMPLE_NAME>
) : DestinationsNavType<ArrayList<$TYPE_ARG_CLASS_SIMPLE_NAME>?>() {

    override fun put(bundle: Bundle, key: String, value: ArrayList<$TYPE_ARG_CLASS_SIMPLE_NAME>?) {
        bundle.putParcelableArrayList(key, value?.toBundleArrayList())
    }

    override fun get(bundle: Bundle, key: String): ArrayList<$TYPE_ARG_CLASS_SIMPLE_NAME>? {
        return bundle.getParcelableArrayList<ParcelableByteArrayList>(key)?.toTypeArrayList()
    }

    override fun parseValue(value: String): ArrayList<$TYPE_ARG_CLASS_SIMPLE_NAME>? {
        return when (value) {
            DECODED_NULL -> null
            "[]" -> arrayListOf()
            else -> {
                val contentValue = value.subSequence(1, value.length - 1)
                val splits = contentValue.split(encodedComma)
                splits.mapTo(ArrayList()) {
                    serializer.fromRouteString(it)
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

    override fun get(navBackStackEntry: NavBackStackEntry, key: String): ArrayList<$TYPE_ARG_CLASS_SIMPLE_NAME>? {
        return navBackStackEntry.arguments?.getParcelableArrayList<ParcelableByteArrayList>(key)?.toTypeArrayList()
    }

    override fun get(savedStateHandle: SavedStateHandle, key: String): ArrayList<$TYPE_ARG_CLASS_SIMPLE_NAME>? {
        return savedStateHandle.get<ArrayList<ParcelableByteArrayList>?>(key)?.toTypeArrayList()
    }

    private fun ArrayList<$TYPE_ARG_CLASS_SIMPLE_NAME>.toBundleArrayList() =
        mapTo(ArrayList()) { ParcelableByteArrayList(serializer.toByteArray(it)) }

    private fun ArrayList<ParcelableByteArrayList>.toTypeArrayList() =
        mapTo(ArrayList()) { serializer.fromByteArray(it.value) }

    private class ParcelableByteArrayList(
        val value: ByteArray
    ): Parcelable {

        constructor(parcel: Parcel) : this(parcel.createByteArray()!!)

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeByteArray(value)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<ParcelableByteArrayList> {
            override fun createFromParcel(parcel: Parcel): ParcelableByteArrayList {
                return ParcelableByteArrayList(parcel)
            }

            override fun newArray(size: Int): Array<ParcelableByteArrayList?> {
                return arrayOfNulls(size)
            }
        }
    }
}
""".trimIndent()

val customTypeArrayListNavTypeTemplate = """
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

val parcelableArrayListNavTypeTemplate = """
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
) : DestinationsNavType<ArrayList<$TYPE_ARG_CLASS_SIMPLE_NAME>?>() {


    override fun put(bundle: Bundle, key: String, value: ArrayList<$TYPE_ARG_CLASS_SIMPLE_NAME>?) {
        bundle.putParcelableArrayList(key, value)
    }

    override fun get(bundle: Bundle, key: String): ArrayList<$TYPE_ARG_CLASS_SIMPLE_NAME>? {
        return bundle.getParcelableArrayList<$TYPE_ARG_CLASS_SIMPLE_NAME>(key)
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
                "[" + value.joinToString(encodedComma) { serializer.toRouteString(it) } + ]"
            )
        }
    }

    override fun get(navBackStackEntry: NavBackStackEntry, key: String): ArrayList<$TYPE_ARG_CLASS_SIMPLE_NAME>? {
        return navBackStackEntry.arguments?.getParcelableArrayList<$TYPE_ARG_CLASS_SIMPLE_NAME>(key)
    }

    override fun get(savedStateHandle: SavedStateHandle, key: String): ArrayList<$TYPE_ARG_CLASS_SIMPLE_NAME>? {
        return savedStateHandle.get<ArrayList<$TYPE_ARG_CLASS_SIMPLE_NAME>?>(key)
    }

}
""".trimIndent()

val serializableArrayListNavTypeTemplate = """
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
) : DestinationsNavType<ArrayList<$TYPE_ARG_CLASS_SIMPLE_NAME>?>() {


    override fun put(bundle: Bundle, key: String, value: ArrayList<$TYPE_ARG_CLASS_SIMPLE_NAME>?) {
        bundle.putSerializable(key, value)
    }

    override fun get(bundle: Bundle, key: String): ArrayList<$TYPE_ARG_CLASS_SIMPLE_NAME>? {
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

    override fun get(navBackStackEntry: NavBackStackEntry, key: String): ArrayList<$TYPE_ARG_CLASS_SIMPLE_NAME>? {
        return navBackStackEntry.arguments?.getSerializable(key) as ArrayList<$TYPE_ARG_CLASS_SIMPLE_NAME>?
    }

    override fun get(savedStateHandle: SavedStateHandle, key: String): ArrayList<$TYPE_ARG_CLASS_SIMPLE_NAME>? {
        return savedStateHandle.get(key)
    }

}
""".trimIndent()
