package com.ramcosta.composedestinations.navargs.primitives

import androidx.core.bundle.Bundle
import androidx.lifecycle.SavedStateHandle
import com.ramcosta.composedestinations.navargs.DestinationsNavType

object DestinationsIntNavType : DestinationsNavType<Int?>() {

    override fun put(bundle: Bundle, key: String, value: Int?) {
        when (val bundleValue = intToBundleValue(value)) {
            is Byte -> bundle.putByte(key, bundleValue)
            is Int -> bundle.putInt(key, bundleValue)
            else -> error("Unexpected type ${bundleValue::class}")
        }
    }

    override fun get(bundle: Bundle, key: String): Int? {
        @Suppress("DEPRECATION")
        return bundleValueToInt(bundle[key])
    }

    override fun parseValue(value: String): Int? {
        return if (value == DECODED_NULL) {
            null
        } else {
            IntType.parseValue(value)
        }
    }

    override fun serializeValue(value: Int?): String {
        return value?.toString() ?: ENCODED_NULL
    }

    override fun get(savedStateHandle: SavedStateHandle, key: String): Int? {
        return bundleValueToInt(savedStateHandle[key])
    }

    override fun put(savedStateHandle: SavedStateHandle, key: String, value: Int?) {
        savedStateHandle[key] = intToBundleValue(value)
    }

    private fun intToBundleValue(value: Int?) = value ?: 0.toByte()

    private fun bundleValueToInt(valueForKey: Any?): Int? {
        return if (valueForKey is Int) {
            valueForKey
        } else {
            null
        }
    }
}