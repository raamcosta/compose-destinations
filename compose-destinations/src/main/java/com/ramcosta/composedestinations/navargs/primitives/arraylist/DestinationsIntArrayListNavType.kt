package com.ramcosta.composedestinations.navargs.primitives.arraylist

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavBackStackEntry
import com.ramcosta.composedestinations.navargs.DestinationsNavType
import com.ramcosta.composedestinations.navargs.primitives.DECODED_NULL
import com.ramcosta.composedestinations.navargs.primitives.ENCODED_NULL

object DestinationsIntArrayListNavType : DestinationsNavType<ArrayList<Int>?>() {

    override fun put(bundle: Bundle, key: String, value: ArrayList<Int>?) {
        bundle.putIntegerArrayList(key, value)
    }

    override fun get(bundle: Bundle, key: String): ArrayList<Int>? {
        return bundle.getIntegerArrayList(key)
    }

    override fun parseValue(value: String): ArrayList<Int>? {
        return if (value == DECODED_NULL) {
            null
        } else {
            value.split(",")
                .mapTo(ArrayList()) { IntType.parseValue(it) }
        }
    }

    override fun serializeValue(value: ArrayList<Int>?): String {
        return value?.joinToString(",") { it.toString() } ?: ENCODED_NULL
    }

    override fun get(navBackStackEntry: NavBackStackEntry, key: String): ArrayList<Int>? {
        return navBackStackEntry.arguments?.getIntegerArrayList(key)
    }

    override fun get(savedStateHandle: SavedStateHandle, key: String): ArrayList<Int>? {
        return savedStateHandle.get(key)
    }
}