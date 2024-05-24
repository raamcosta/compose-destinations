package com.ramcosta.composedestinations.navargs.primitives

import androidx.core.bundle.Bundle
import androidx.lifecycle.SavedStateHandle
import com.ramcosta.composedestinations.navargs.DestinationsNavType

object DestinationsLongNavType : DestinationsNavType<Long?>() {

    override fun put(bundle: Bundle, key: String, value: Long?) {
        when (val bundleValue = longToBundleValue(value)) {
            is Byte -> bundle.putByte(key, bundleValue)
            is Long -> bundle.putLong(key, bundleValue)
            else -> error("Unexpected type ${bundleValue::class}")
        }
    }

    override fun get(bundle: Bundle, key: String): Long? {
        @Suppress("DEPRECATION")
        return bundleValueToLong(bundle[key])
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
        return bundleValueToLong(savedStateHandle[key])
    }

    override fun put(savedStateHandle: SavedStateHandle, key: String, value: Long?) {
        savedStateHandle[key] = longToBundleValue(value)
    }

    private fun longToBundleValue(value: Long?) = value ?: 0.toByte()

    private fun bundleValueToLong(valueForKey: Any?): Long? {
        return if (valueForKey is Long) {
            valueForKey
        } else {
            null
        }
    }
}