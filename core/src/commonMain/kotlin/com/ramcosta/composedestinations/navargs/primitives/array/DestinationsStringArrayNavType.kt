package com.ramcosta.composedestinations.navargs.primitives.array

import androidx.core.bundle.Bundle
import androidx.lifecycle.SavedStateHandle
import com.ramcosta.composedestinations.navargs.DestinationsNavType
import com.ramcosta.composedestinations.navargs.primitives.DECODED_NULL
import com.ramcosta.composedestinations.navargs.primitives.DestinationsStringNavType
import com.ramcosta.composedestinations.navargs.primitives.ENCODED_NULL
import com.ramcosta.composedestinations.navargs.primitives.encodedComma
import com.ramcosta.composedestinations.navargs.utils.encodeForRoute

object DestinationsStringArrayNavType : DestinationsNavType<Array<String>?>() {

    override fun put(bundle: Bundle, key: String, value: Array<String>?) {
        bundle.putStringArray(key, value)
    }

    override fun get(bundle: Bundle, key: String): Array<String>? {
        return bundle.getStringArray(key)
    }

    override fun parseValue(value: String): Array<String>? {
        return when (value) {
            DECODED_NULL -> null
            "[]" -> arrayOf()
            else -> value
                .subSequence(1, value.length - 1)
                .split(encodedComma).let { splits ->
                    Array(splits.size) {
                        when (val split = splits[it]) {
                            DestinationsStringNavType.DECODED_EMPTY_STRING -> ""
                            else -> split
                        }
                    }
                }
        }
    }

    override fun serializeValue(value: Array<String>?): String {
        return when (value) {
            null -> ENCODED_NULL
            else -> encodeForRoute(
                "[" + value.joinToString(encodedComma) {
                    it.ifEmpty { DestinationsStringNavType.ENCODED_EMPTY_STRING }
                } + "]"
            )
        }
    }

    override fun get(savedStateHandle: SavedStateHandle, key: String): Array<String>? {
        return savedStateHandle[key]
    }

    override fun put(savedStateHandle: SavedStateHandle, key: String, value: Array<String>?) {
        savedStateHandle[key] = value
    }
}