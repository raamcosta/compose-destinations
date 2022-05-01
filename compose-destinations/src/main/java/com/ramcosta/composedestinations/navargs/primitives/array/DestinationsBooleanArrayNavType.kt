package com.ramcosta.composedestinations.navargs.primitives.array

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavBackStackEntry
import com.ramcosta.composedestinations.navargs.DestinationsNavType
import com.ramcosta.composedestinations.navargs.primitives.DECODED_NULL
import com.ramcosta.composedestinations.navargs.primitives.ENCODED_NULL
import com.ramcosta.composedestinations.navargs.primitives.encodedComma

object DestinationsBooleanArrayNavType : DestinationsNavType<BooleanArray?>() {

    override fun put(bundle: Bundle, key: String, value: BooleanArray?) {
        bundle.putBooleanArray(key, value)
    }

    override fun get(bundle: Bundle, key: String): BooleanArray? {
        return bundle.getBooleanArray(key)
    }

    override fun parseValue(value: String): BooleanArray? {
        return if (value == DECODED_NULL) {
            null
        } else {
            val splits = if (value.contains(encodedComma)) {
                value.split(encodedComma)
            } else {
                value.split(",")
            }

            BooleanArray(splits.size) { BoolType.parseValue(splits[it]) }
        }
    }

    override fun serializeValue(value: BooleanArray?): String {
        return value?.joinToString(",") { it.toString() } ?: ENCODED_NULL
    }

    override fun get(navBackStackEntry: NavBackStackEntry, key: String): BooleanArray? {
        return navBackStackEntry.arguments?.getBooleanArray(key)
    }

    override fun get(savedStateHandle: SavedStateHandle, key: String): BooleanArray? {
        return savedStateHandle.get(key)
    }

}

