package com.ramcosta.composedestinations.codegen.templates.navtype.arrays

import com.ramcosta.composedestinations.codegen.codeGenBasePackageName
import com.ramcosta.composedestinations.codegen.commons.CORE_PACKAGE_NAME
import com.ramcosta.composedestinations.codegen.templates.core.FileTemplate
import com.ramcosta.composedestinations.codegen.templates.core.setOfImportable

val ktxSerializableArrayListNavTypeTemplate = FileTemplate(
    packageStatement = "package $codeGenBasePackageName.navtype",
    imports = setOfImportable(
        "android.os.Bundle",
        "android.os.Parcel",
        "android.os.Parcelable",
        "androidx.lifecycle.SavedStateHandle",
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
)