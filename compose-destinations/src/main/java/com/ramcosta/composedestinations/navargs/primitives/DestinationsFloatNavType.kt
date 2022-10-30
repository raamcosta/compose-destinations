package com.ramcosta.composedestinations.navargs.primitives

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavBackStackEntry
import com.ramcosta.composedestinations.navargs.DestinationsNavType

object DestinationsFloatNavType : DestinationsNavType<Float?>() {

    override fun put(bundle: Bundle, key: String, value: Float?) {
        if (value == null) {
            bundle.putByte(key, 0)
        } else {
            bundle.putFloat(key, value)
        }
    }

    override fun get(bundle: Bundle, key: String): Float? {
        return floatValue(bundle[key])
    }

    override fun parseValue(value: String): Float? {
        return if (value == DECODED_NULL) {
            null
        } else {
            FloatType.parseValue(value)
        }
    }

    override fun serializeValue(value: Float?): String {
        return value?.toString() ?: ENCODED_NULL
    }

    override fun get(savedStateHandle: SavedStateHandle, key: String): Float? {
        return floatValue(savedStateHandle.get<Any?>(key))
    }

    private fun floatValue(valueForKey: Any?): Float? {
        return if (valueForKey is Float) {
            valueForKey
        } else {
            null
        }
    }
}