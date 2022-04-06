package com.ramcosta.composedestinations.navargs.ktxserializable

import com.ramcosta.composedestinations.navargs.DestinationsNavTypeSerializer
import com.ramcosta.composedestinations.navargs.utils.base64ToByteArray
import com.ramcosta.composedestinations.navargs.utils.toBase64Str
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream


/**
 * Default [DestinationsNavTypeSerializer] for Kotlin [Serializable]s which converts them to a
 * Base64 string and then parses them back.
 *
 * This gets used by the generated code if you don't provide an explicit
 * [DestinationsNavTypeSerializer] annotated with `@NavTypeSerializer` for the type being
 * passed as navigation argument.
 */
@ExperimentalSerializationApi
class DefaultKtxSerializableNavTypeSerializer<T : @Serializable Any>(
    private val serializer: KSerializer<T>,
) :
    DestinationsNavTypeSerializer<T> {
    override fun toRouteString(value: T): String {
        return ByteArrayOutputStream().use {
            Json.encodeToStream(serializer, value, it)
            it.toByteArray().toBase64Str()
        }
    }

    override fun fromRouteString(routeStr: String): T {
        val bytes = routeStr.base64ToByteArray()
        return ByteArrayInputStream(bytes).use {
            Json.decodeFromStream(serializer, it)
        }
    }
}
