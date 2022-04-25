package com.ramcosta.composedestinations.navargs.primitives.arraylist

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavBackStackEntry
import com.ramcosta.composedestinations.navargs.DestinationsNavType
import com.ramcosta.composedestinations.navargs.primitives.DECODED_NULL
import com.ramcosta.composedestinations.navargs.primitives.ENCODED_NULL

object DestinationsBooleanArrayListNavType : DestinationsNavType<ArrayList<Boolean>?>() {

    override fun put(bundle: Bundle, key: String, value: ArrayList<Boolean>?) {
        bundle.putBooleanArray(key, value?.let { list -> BooleanArray(list.size) { list[it] } })
    }

    override fun get(bundle: Bundle, key: String): ArrayList<Boolean>? {
        return bundle.getBooleanArray(key).toArrayList()
    }

    override fun parseValue(value: String): ArrayList<Boolean>? {
        return if (value == DECODED_NULL) {
            null
        } else {
            value.split(",")
                .mapTo(ArrayList()) {
                    BoolType.parseValue(it)
                }
        }
    }

    override fun serializeValue(value: ArrayList<Boolean>?): String {
        return value?.joinToString(",") { it.toString() } ?: ENCODED_NULL
    }

    override fun get(navBackStackEntry: NavBackStackEntry, key: String): ArrayList<Boolean>? {
        return navBackStackEntry.arguments?.getBooleanArray(key).toArrayList()
    }

    override fun get(savedStateHandle: SavedStateHandle, key: String): ArrayList<Boolean>? {
        return savedStateHandle.get<BooleanArray?>(key).toArrayList()
    }

    private fun BooleanArray?.toArrayList(): ArrayList<Boolean>? {
        return this?.let { ArrayList(it.toList()) }
    }
}

