package com.ramcosta.composedestinations.navargs.primitives

import androidx.core.bundle.Bundle
import androidx.lifecycle.SavedStateHandle
import com.ramcosta.composedestinations.navargs.DestinationsNavType

class DestinationsEnumNavType<E : Enum<*>>(
    private val converter: (String) -> E
) : DestinationsNavType<E?>() {

    override fun put(bundle: Bundle, key: String, value: E?) {
        bundle.putString(key, serializeValue(value))
    }

    override fun get(bundle: Bundle, key: String): E? {
        return bundle.getString(key)?.let { converter(it) }
    }

    override fun parseValue(value: String): E? {
        if (value == DECODED_NULL) return null

        return converter(value)
    }

    override fun serializeValue(value: E?): String {
        return value?.name ?: ENCODED_NULL
    }

    override fun get(savedStateHandle: SavedStateHandle, key: String): E? {
        return savedStateHandle.get<String>(key)?.let(converter)
    }

    override fun put(savedStateHandle: SavedStateHandle, key: String, value: E?) {
        savedStateHandle[key] = value?.name
    }

}

