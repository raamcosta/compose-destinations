package com.ramcosta.composedestinations.navargs.primitives.array

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavBackStackEntry
import com.ramcosta.composedestinations.navargs.DestinationsNavType
import com.ramcosta.composedestinations.navargs.primitives.DECODED_NULL
import com.ramcosta.composedestinations.navargs.primitives.ENCODED_NULL

object DestinationsIntArrayNavType : DestinationsNavType<IntArray?>() {

    override fun put(bundle: Bundle, key: String, value: IntArray?) {
        bundle.putIntArray(key, value)
    }

    override fun get(bundle: Bundle, key: String): IntArray? {
        return bundle.getIntArray(key)
    }

    override fun parseValue(value: String): IntArray? {
        return if (value == DECODED_NULL) {
            null
        } else {
            val splits = value.split(",")
            IntArray(splits.size) { IntType.parseValue(splits[it]) }
        }
    }

    override fun serializeValue(value: IntArray?): String {
        return value?.joinToString(",") { it.toString() } ?: ENCODED_NULL
    }

    override fun get(navBackStackEntry: NavBackStackEntry, key: String): IntArray? {
        return navBackStackEntry.arguments?.getIntArray(key)
    }

    override fun get(savedStateHandle: SavedStateHandle, key: String): IntArray? {
        return savedStateHandle.get(key)
    }
}