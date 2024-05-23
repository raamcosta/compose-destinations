package com.ramcosta.composedestinations.navargs.primitives

import androidx.core.bundle.Bundle
import androidx.lifecycle.SavedStateHandle
import com.ramcosta.composedestinations.navargs.DestinationsNavType

object DestinationsBooleanNavType : DestinationsNavType<Boolean?>() {

    override fun put(bundle: Bundle, key: String, value: Boolean?) {
        when (val bundleValue = booleanToBundleValue(value)) {
            is Byte -> bundle.putByte(key, bundleValue)
            is Boolean -> bundle.putBoolean(key, bundleValue)
            else -> error("Unexpected type ${bundleValue::class}")
        }
    }

    override fun get(bundle: Bundle, key: String): Boolean? {
        @Suppress("DEPRECATION")
        return bundleValueToBoolean(bundle[key])
    }

    override fun parseValue(value: String): Boolean? {
        return if (value == DECODED_NULL) {
            null
        } else {
            BoolType.parseValue(value)
        }
    }

    override fun serializeValue(value: Boolean?): String {
        return value?.toString() ?: ENCODED_NULL
    }

    override fun get(savedStateHandle: SavedStateHandle, key: String): Boolean? {
        return bundleValueToBoolean(savedStateHandle[key])
    }

    override fun put(savedStateHandle: SavedStateHandle, key: String, value: Boolean?) {
        savedStateHandle[key] = booleanToBundleValue(value)
    }

    private fun booleanToBundleValue(value: Boolean?) = value ?: 0.toByte()

    private fun bundleValueToBoolean(valueForKey: Any?): Boolean? {
        return if (valueForKey is Boolean) {
            valueForKey
        } else {
            null
        }
    }
}