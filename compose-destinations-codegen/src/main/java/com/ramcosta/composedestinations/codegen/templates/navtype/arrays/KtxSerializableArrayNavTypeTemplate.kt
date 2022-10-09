package com.ramcosta.composedestinations.codegen.templates.navtype.arrays

import com.ramcosta.composedestinations.codegen.codeGenBasePackageName
import com.ramcosta.composedestinations.codegen.commons.CORE_PACKAGE_NAME
import com.ramcosta.composedestinations.codegen.templates.core.FileTemplate
import com.ramcosta.composedestinations.codegen.templates.core.setOfImportable

val ktxSerializableArrayNavTypeTemplate = FileTemplate(
    packageStatement = "package $codeGenBasePackageName.navtype",
    imports = setOfImportable(
        "android.os.Bundle",
        "android.os.Parcel",
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
        "kotlinx.serialization.ExperimentalSerializationApi",
    ),
    sourceCode = """
@OptIn(ExperimentalSerializationApi::class)
$NAV_TYPE_INITIALIZATION_CODE
@OptIn(ExperimentalSerializationApi::class)
public class $ARRAY_CUSTOM_NAV_TYPE_NAME(
    private val serializer: DefaultKtxSerializableNavTypeSerializer<$TYPE_ARG_CLASS_SIMPLE_NAME>
) : DestinationsNavType<Array<$TYPE_ARG_CLASS_SIMPLE_NAME>?>() {

    override fun put(bundle: Bundle, key: String, value: Array<$TYPE_ARG_CLASS_SIMPLE_NAME>?) {
        bundle.putParcelableArray(key, value?.toBundleArray())
    }

    override fun get(bundle: Bundle, key: String): Array<$TYPE_ARG_CLASS_SIMPLE_NAME>? {
        @Suppress("DEPRECATION")
        return bundle.getParcelableArray(key)?.toTypeArray()
    }

    override fun parseValue(value: String): Array<$TYPE_ARG_CLASS_SIMPLE_NAME>? {
        return when (value) {
            DECODED_NULL -> null
            "[]" -> arrayOf()
            else -> {
                value
                    .subSequence(1, value.length - 1)
                    .split(encodedComma).map {
                        serializer.fromRouteString(it)
                    }.toTypedArray()
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
        @Suppress("DEPRECATION")
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
)