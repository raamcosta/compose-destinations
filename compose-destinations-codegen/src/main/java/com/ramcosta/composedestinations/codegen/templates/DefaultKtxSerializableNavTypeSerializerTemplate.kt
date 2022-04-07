package com.ramcosta.composedestinations.codegen.templates

import com.ramcosta.composedestinations.codegen.codeGenBasePackageName

val defaultKtxSerializableNavTypeSerializerTemplatePkg =
    "$codeGenBasePackageName.navargs.ktxserializable"
const val DEFAULT_KTX_SERIALIZABLE_NAV_TYPE_SERIALIZER_TEMPLATE_NAME =
    "DefaultKtxSerializableNavTypeSerializer"

val defaultKtxSerializableNavTypeSerializerTemplate = """
package $defaultKtxSerializableNavTypeSerializerTemplatePkg

import android.util.Base64
import com.ramcosta.composedestinations.navargs.DestinationsNavTypeSerializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset

/**
 * Default [DestinationsNavTypeSerializer] for Kotlin [Serializable]s which converts them to a
 * Base64 string and then parses them back.
 *
 * This gets used by the generated code if you don't provide an explicit
 * [DestinationsNavTypeSerializer] annotated with `@NavTypeSerializer` for the type being
 * passed as navigation argument.
 */
@ExperimentalSerializationApi
class $DEFAULT_KTX_SERIALIZABLE_NAV_TYPE_SERIALIZER_TEMPLATE_NAME<T : @Serializable Any>(
    private val serializer: KSerializer<T>,
) : DestinationsNavTypeSerializer<T> {

    override fun toRouteString(value: T): String {
        return toByteArray(value).toBase64Str()
    }

    override fun fromRouteString(routeStr: String): T {
        val bytes = routeStr.base64ToByteArray()
        return fromByteArray(bytes)
    }
    
    fun toByteArray(value: T): ByteArray =
        ByteArrayOutputStream().use {
            Json.encodeToStream(serializer, value, it)
            it.toByteArray()
        }
    
    fun fromByteArray(bytes: ByteArray): T =
        ByteArrayInputStream(bytes).use {
            Json.decodeFromStream(serializer, it)
        }

    private fun String.base64ToByteArray(): ByteArray {
        return Base64.decode(
            toByteArray(Charset.defaultCharset()),
            Base64.URL_SAFE or Base64.NO_WRAP
        )
    }

    private fun ByteArray.toBase64Str(): String {
        return Base64.encodeToString(this, Base64.URL_SAFE or Base64.NO_WRAP)
    }

}

""".trimIndent()
