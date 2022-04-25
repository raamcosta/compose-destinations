package com.ramcosta.composedestinations.navargs.primitives.array

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavBackStackEntry
import com.ramcosta.composedestinations.navargs.DestinationsNavType
import com.ramcosta.composedestinations.navargs.primitives.DECODED_NULL
import com.ramcosta.composedestinations.navargs.primitives.ENCODED_NULL

object DestinationsLongArrayNavType : DestinationsNavType<LongArray?>() {

    override fun put(bundle: Bundle, key: String, value: LongArray?) {
        bundle.putLongArray(key, value)
    }

    override fun get(bundle: Bundle, key: String): LongArray? {
        return bundle.getLongArray(key)
    }

    override fun parseValue(value: String): LongArray? {
        return if (value == DECODED_NULL) {
            null
        } else {
            val splits = value.split(",")
            LongArray(splits.size) { LongType.parseValue(splits[it]) }
        }
    }

    override fun serializeValue(value: LongArray?): String {
        return value?.joinToString(",") { it.toString() } ?: ENCODED_NULL
    }

    override fun get(navBackStackEntry: NavBackStackEntry, key: String): LongArray? {
        return navBackStackEntry.arguments?.getLongArray(key)
    }

    override fun get(savedStateHandle: SavedStateHandle, key: String): LongArray? {
        return savedStateHandle.get(key)
    }
}