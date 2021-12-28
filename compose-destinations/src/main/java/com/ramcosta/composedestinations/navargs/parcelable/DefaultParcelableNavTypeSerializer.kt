package com.ramcosta.composedestinations.navargs.parcelable

import android.os.BadParcelableException
import android.os.Parcel
import android.os.Parcelable
import com.ramcosta.composedestinations.utils.base64ToByteArray
import com.ramcosta.composedestinations.utils.toBase64Str

/**
 * Default [ParcelableNavTypeSerializer] which converts the parcelable to Base64 strings
 * and then parses them back.
 *
 * This gets used by the generated code if you don't provide an explicit
 * [ParcelableNavTypeSerializer] annotated with `@NavTypeSerializer` for the type being
 * passed as navigation argument.
 */
class DefaultParcelableNavTypeSerializer : ParcelableNavTypeSerializer<Parcelable> {

    override fun toRouteString(value: Parcelable): String {
        return value.toBase64()
    }

    override fun fromRouteString(routeStr: String, jClass: Class<out Parcelable>): Parcelable {
        return base64ToParcelable(routeStr, jClass)
    }

    private fun Parcelable.toBase64(): String {
        val parcel = Parcel.obtain()
        writeToParcel(parcel, 0)
        val bytes = parcel.marshall()
        parcel.recycle()

        return bytes.toBase64Str()
    }

    private fun <T> base64ToParcelable(base64: String, jClass: Class<T>): T {
        val bytes = base64.base64ToByteArray()
        val parcel = unmarshall(bytes)
        val result = jClass.parcelableCreator.createFromParcel(parcel)
        parcel.recycle()
        return result
    }

    private fun unmarshall(bytes: ByteArray): Parcel {
        val parcel = Parcel.obtain()
        parcel.unmarshall(bytes, 0, bytes.size)
        parcel.setDataPosition(0)
        return parcel
    }

    @Suppress("UNCHECKED_CAST")
    private val <T> Class<T>.parcelableCreator
        get() : Parcelable.Creator<T> {
            return try {
                val creatorField = getField("CREATOR")
                creatorField.get(null) as Parcelable.Creator<T>
            } catch (e: Exception) {
                throw BadParcelableException(e)
            } catch (t: Throwable) {
                throw BadParcelableException(t.message)
            }
        }
}
