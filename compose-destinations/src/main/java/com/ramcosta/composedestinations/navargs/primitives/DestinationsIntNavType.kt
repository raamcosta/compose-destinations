package com.ramcosta.composedestinations.navargs.primitives

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavBackStackEntry
import com.ramcosta.composedestinations.navargs.DestinationsNavType

object DestinationsIntNavType : DestinationsNavType<Int?>() {

    override fun put(bundle: Bundle, key: String, value: Int?) {
        if (value == null) {
            bundle.putByte(key, 0)
        } else {
            bundle.putInt(key, value)
        }
    }

    override fun get(bundle: Bundle, key: String): Int? {
        return intValue(bundle[key])
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

    fun get(navBackStackEntry: NavBackStackEntry, key: String): Int? {
        return intValue(navBackStackEntry.arguments?.get(key))
    }

    fun get(savedStateHandle: SavedStateHandle, key: String): Int? {
        return intValue(savedStateHandle.get<Any?>(key))
    }

    private fun intValue(valueForKey: Any?): Int? {
        return if (valueForKey is Int) {
            valueForKey
        } else {
            null
        }
    }
}