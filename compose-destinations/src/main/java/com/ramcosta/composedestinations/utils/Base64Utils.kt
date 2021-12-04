package com.ramcosta.composedestinations.utils

import android.os.Parcel
import android.os.Parcelable
import android.util.Base64
import java.io.*
import java.nio.charset.Charset

/**
 * Utilities to convert [Parcelable] (or [Serializable]) to Base64 strings and
 * Base64 Strings back to the [Parcelable] (or [Serializable]).
 *
 * They'll be used internally by the generated code, if you have [Parcelable]
 * or [Serializable] navigation arguments.
 */
object Base64Utils {

    fun Parcelable.toBase64(): String {
        val parcel = Parcel.obtain()
        writeToParcel(parcel, 0)
        val bytes = parcel.marshall()
        parcel.recycle()

        return bytes.toBase64()
    }

    fun <T> base64ToParcelable(base64: String, creator: Parcelable.Creator<T>): T {
        val bytes = base64.base64ToByteArray()
        val parcel = unmarshall(bytes)
        val result = creator.createFromParcel(parcel)
        parcel.recycle()
        return result
    }

    fun Serializable.toBase64(): String {
        return ByteArrayOutputStream().use {
            val out = ObjectOutputStream(it)
            out.writeObject(this)
            out.flush()
            it.toByteArray().toBase64()
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> base64ToSerializable(base64: String): T {
        val bytes = base64.base64ToByteArray()
        return ObjectInputStream(ByteArrayInputStream(bytes)).use {
            it.readObject() as T
        }
    }

    private fun unmarshall(bytes: ByteArray): Parcel {
        val parcel = Parcel.obtain()
        parcel.unmarshall(bytes, 0, bytes.size)
        parcel.setDataPosition(0)
        return parcel
    }

    private fun String.base64ToByteArray(): ByteArray {
        return Base64.decode(toByteArray(Charset.defaultCharset()), Base64.URL_SAFE or Base64.NO_WRAP)
    }

    private fun ByteArray.toBase64(): String {
        return Base64.encodeToString(this, Base64.URL_SAFE or Base64.NO_WRAP)
    }
}