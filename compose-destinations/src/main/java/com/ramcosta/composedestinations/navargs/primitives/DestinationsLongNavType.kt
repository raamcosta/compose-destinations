package com.ramcosta.composedestinations.navargs.primitives

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavBackStackEntry
import com.ramcosta.composedestinations.navargs.DestinationsNavType

object DestinationsLongNavType : DestinationsNavType<Long?>() {

    override fun put(bundle: Bundle, key: String, value: Long?) {
        if (value == null) {
            bundle.putByte(key, 0)
        } else {
            bundle.putLong(key, value)
        }
    }

    override fun get(bundle: Bundle, key: String): Long? {
        @Suppress("DEPRECATION")
        return longValue(bundle[key])
    }

    override fun parseValue(value: String): Long? {
        return if (value == DECODED_NULL) {
            null
        } else {
            LongType.parseValue(value)
        }
    }

    override fun serializeValue(value: Long?): String {
        return value?.toString() ?: ENCODED_NULL
    }

    override fun get(savedStateHandle: SavedStateHandle, key: String): Long? {
        return longValue(savedStateHandle.get<Any?>(key))
    }

    private fun longValue(valueForKey: Any?): Long? {
        return if (valueForKey is Long) {
            valueForKey
        } else {
            null
        }
    }
}