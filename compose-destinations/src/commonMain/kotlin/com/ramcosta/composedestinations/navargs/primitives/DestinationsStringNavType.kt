package com.ramcosta.composedestinations.navargs.primitives

import androidx.core.bundle.Bundle
import androidx.lifecycle.SavedStateHandle
import com.ramcosta.composedestinations.navargs.DestinationsNavType
import com.ramcosta.composedestinations.navargs.utils.encodeForRoute

object DestinationsStringNavType : DestinationsNavType<String?>() {

    internal const val ENCODED_EMPTY_STRING = "%02%03"
    internal const val DECODED_EMPTY_STRING: String = "\u0002\u0003"

    private const val ENCODED_DEFAULT_VALUE_STRING_PREFIX = "%02def%03"
    private const val DECODED_DEFAULT_VALUE_STRING_PREFIX: String = "\u0002def\u0003"

    override fun put(bundle: Bundle, key: String, value: String?) {
        StringType.put(bundle, key, value)
    }

    override fun get(bundle: Bundle, key: String): String? {
        return StringType[bundle, key]
    }

    override fun parseValue(value: String): String? {
        if (value.startsWith(DECODED_DEFAULT_VALUE_STRING_PREFIX)) {
            return value.removePrefix(DECODED_DEFAULT_VALUE_STRING_PREFIX)
        }

        return when (value) {
            DECODED_NULL -> null
            DECODED_EMPTY_STRING -> ""
            else -> value
        }
    }

    override fun serializeValue(value: String?): String {
        return when {
            value == null -> ENCODED_NULL
            value.isEmpty() -> ENCODED_EMPTY_STRING
            else -> encodeForRoute(value)
        }
    }

    fun serializeValue(argName: String, value: String?): String {
        if ("{$argName}" == value) {
            return "$ENCODED_DEFAULT_VALUE_STRING_PREFIX${encodeForRoute(value)}"
        }

        return serializeValue(value)
    }

    override fun get(savedStateHandle: SavedStateHandle, key: String): String? {
        return savedStateHandle[key]
    }

    override fun put(savedStateHandle: SavedStateHandle, key: String, value: String?) {
        savedStateHandle[key] = value
    }
}
