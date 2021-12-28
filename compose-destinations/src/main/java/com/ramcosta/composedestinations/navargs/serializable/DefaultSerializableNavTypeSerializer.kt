package com.ramcosta.composedestinations.navargs.serializable

import com.ramcosta.composedestinations.utils.base64ToByteArray
import com.ramcosta.composedestinations.utils.toBase64Str
import java.io.*

/**
 * Default [SerializableNavTypeSerializer] which converts the [Serializable] to Base64 strings
 * and then parses them back.
 *
 * This gets used by the generated code if you don't provide an explicit
 * [SerializableNavTypeSerializer] annotated with `@NavTypeSerializer` for the type being
 * passed as navigation argument.
 */
class DefaultSerializableNavTypeSerializer : SerializableNavTypeSerializer<Serializable> {

    override fun toRouteString(value: Serializable): String {
        return value.toBase64()
    }

    override fun fromRouteString(routeStr: String): Serializable {
        return base64ToSerializable(routeStr)
    }

    private fun Serializable.toBase64(): String {
        return ByteArrayOutputStream().use {
            val out = ObjectOutputStream(it)
            out.writeObject(this)
            out.flush()
            it.toByteArray().toBase64Str()
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> base64ToSerializable(base64: String): T {
        val bytes = base64.base64ToByteArray()
        return ObjectInputStream(ByteArrayInputStream(bytes)).use {
            it.readObject() as T
        }
    }
}