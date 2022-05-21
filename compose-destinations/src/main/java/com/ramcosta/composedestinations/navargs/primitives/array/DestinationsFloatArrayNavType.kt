package com.ramcosta.composedestinations.navargs.primitives.array

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavBackStackEntry
import com.ramcosta.composedestinations.navargs.DestinationsNavType
import com.ramcosta.composedestinations.navargs.primitives.DECODED_NULL
import com.ramcosta.composedestinations.navargs.primitives.ENCODED_NULL
import com.ramcosta.composedestinations.navargs.primitives.encodedComma

object DestinationsFloatArrayNavType : DestinationsNavType<FloatArray?>() {

    override fun put(bundle: Bundle, key: String, value: FloatArray?) {
        bundle.putFloatArray(key, value)
    }

    override fun get(bundle: Bundle, key: String): FloatArray? {
        return bundle.getFloatArray(key)
    }

    override fun parseValue(value: String): FloatArray? {
        return when (value) {
            DECODED_NULL -> null
            "[]" -> floatArrayOf()
            else -> {
                val contentValue = value.subSequence(1, value.length - 1)
                val splits = if (contentValue.contains(encodedComma)) {
                    contentValue.split(encodedComma)
                } else {
                    contentValue.split(",")
                }

                FloatArray(splits.size) { FloatType.parseValue(splits[it]) }
            }
        }
    }

    override fun serializeValue(value: FloatArray?): String {
        value ?: return ENCODED_NULL
        return "[${value.joinToString(",") { it.toString() }}]"
    }

    override fun get(navBackStackEntry: NavBackStackEntry, key: String): FloatArray? {
        return navBackStackEntry.arguments?.getFloatArray(key)
    }

    override fun get(savedStateHandle: SavedStateHandle, key: String): FloatArray? {
        return savedStateHandle.get(key)
    }
}