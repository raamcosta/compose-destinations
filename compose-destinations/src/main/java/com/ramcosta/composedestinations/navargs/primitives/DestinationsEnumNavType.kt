package com.ramcosta.composedestinations.navargs.primitives

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavBackStackEntry
import com.ramcosta.composedestinations.navargs.DestinationsNavType

@Suppress("UNCHECKED_CAST")
class DestinationsEnumNavType<E : Enum<*>>(
    private val enumType: Class<E>
) : DestinationsNavType<E?>() {

    override fun put(bundle: Bundle, key: String, value: E?) {
        bundle.putSerializable(key, value)
    }

    override fun get(bundle: Bundle, key: String): E? {
        @Suppress("DEPRECATION")
        return bundle.getSerializable(key) as E?
    }

    override fun parseValue(value: String): E? {
        if (value == DECODED_NULL) return null

        return enumType.valueOfIgnoreCase(value)
    }

    override fun serializeValue(value: E?): String {
        return value?.name ?: ENCODED_NULL
    }

    override fun get(savedStateHandle: SavedStateHandle, key: String): E? {
        return savedStateHandle.get<E?>(key)
    }

}

