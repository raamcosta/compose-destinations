package com.ramcosta.composedestinations.navargs.serializable

import com.ramcosta.composedestinations.navargs.DestinationsNavTypeSerializer
import com.ramcosta.composedestinations.navargs.utils.base64ToByteArray
import com.ramcosta.composedestinations.navargs.utils.toBase64Str
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable

/**
 * Default [DestinationsNavTypeSerializer] for [Serializable]s which converts them to Base64 strings
 * and then parses them back.
 *
 * This gets used by the generated code if you don't provide an explicit
 * [DestinationsNavTypeSerializer] annotated with `@NavTypeSerializer` for the type being
 * passed as navigation argument.
 */
class DefaultSerializableNavTypeSerializer : DestinationsNavTypeSerializer<Serializable> {
    //TODO RACOSTA, should be on android src set, given it's on jvm target :thinking:
    // A: https://kotlinlang.slack.com/archives/C3PQML5NU/p1716459036428389
    // solution is to create my own src set for both jvm and Android, put this there instead

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