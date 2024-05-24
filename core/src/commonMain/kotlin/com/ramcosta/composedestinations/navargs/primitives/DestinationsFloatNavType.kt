package com.ramcosta.composedestinations.navargs.primitives

import androidx.core.bundle.Bundle
import androidx.lifecycle.SavedStateHandle
import com.ramcosta.composedestinations.navargs.DestinationsNavType

object DestinationsFloatNavType : DestinationsNavType<Float?>() {

    override fun put(bundle: Bundle, key: String, value: Float?) {
        when (val bundleValue = floatToBundleValue(value)) {
            is Byte -> bundle.putByte(key, bundleValue)
            is Float -> bundle.putFloat(key, bundleValue)
            else -> error("Unexpected type ${bundleValue::class}")
        }
    }

    override fun get(bundle: Bundle, key: String): Float? {
        @Suppress("DEPRECATION")
        return bundleValueToFloat(bundle[key])
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
        return bundleValueToFloat(savedStateHandle[key])
    }

    override fun put(savedStateHandle: SavedStateHandle, key: String, value: Float?) {
        savedStateHandle[key] = floatToBundleValue(value)
    }

    private fun floatToBundleValue(value: Float?) = value ?: 0.toByte()

    private fun bundleValueToFloat(valueForKey: Any?): Float? {
        return if (valueForKey is Float) {
            valueForKey
        } else {
            null
        }
    }
}