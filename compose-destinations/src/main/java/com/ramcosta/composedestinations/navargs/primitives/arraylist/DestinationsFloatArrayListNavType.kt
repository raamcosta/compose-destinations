package com.ramcosta.composedestinations.navargs.primitives.arraylist

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavBackStackEntry
import com.ramcosta.composedestinations.navargs.DestinationsNavType
import com.ramcosta.composedestinations.navargs.primitives.DECODED_NULL
import com.ramcosta.composedestinations.navargs.primitives.ENCODED_NULL

object DestinationsFloatArrayListNavType : DestinationsNavType<ArrayList<Float>?>() {

    override fun put(bundle: Bundle, key: String, value: ArrayList<Float>?) {
        bundle.putFloatArray(key, value?.let { list -> FloatArray(list.size) { list[it] } })
    }

    override fun get(bundle: Bundle, key: String): ArrayList<Float>? {
        return bundle.getFloatArray(key)?.toList()?.let { ArrayList(it) }
    }

    override fun parseValue(value: String): ArrayList<Float>? {
        return if (value == DECODED_NULL) {
            null
        } else {
            value.split(",")
                .mapTo(ArrayList()) { FloatType.parseValue(it) }
        }
    }

    override fun serializeValue(value: ArrayList<Float>?): String {
        return value?.joinToString(",") { it.toString() } ?: ENCODED_NULL
    }

    override fun get(navBackStackEntry: NavBackStackEntry, key: String): ArrayList<Float>? {
        return navBackStackEntry.arguments?.getFloatArray(key).toArrayList()
    }

    override fun get(savedStateHandle: SavedStateHandle, key: String): ArrayList<Float>? {
        return savedStateHandle.get<FloatArray?>(key).toArrayList()
    }

    private fun FloatArray?.toArrayList(): ArrayList<Float>? {
        return this?.let { ArrayList(it.toList()) }
    }
}