package com.ramcosta.composedestinations.navargs.primitives.array

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavBackStackEntry
import com.ramcosta.composedestinations.navargs.DestinationsNavType
import com.ramcosta.composedestinations.navargs.primitives.DECODED_NULL
import com.ramcosta.composedestinations.navargs.primitives.ENCODED_NULL

object DestinationsFloatArrayNavType : DestinationsNavType<FloatArray?>() {

    override fun put(bundle: Bundle, key: String, value: FloatArray?) {
        bundle.putFloatArray(key, value)
    }

    override fun get(bundle: Bundle, key: String): FloatArray? {
        return bundle.getFloatArray(key)
    }

    override fun parseValue(value: String): FloatArray? {
        return if (value == DECODED_NULL) {
            null
        } else {
            val splits = value.split(",")
            FloatArray(splits.size) { FloatType.parseValue(splits[it]) }
        }
    }

    override fun serializeValue(value: FloatArray?): String {
        return value?.joinToString(",") { it.toString() } ?: ENCODED_NULL
    }

    override fun get(navBackStackEntry: NavBackStackEntry, key: String): FloatArray? {
        return navBackStackEntry.arguments?.getFloatArray(key)
    }

    override fun get(savedStateHandle: SavedStateHandle, key: String): FloatArray? {
        return savedStateHandle.get(key)
    }
}