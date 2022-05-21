package com.ramcosta.composedestinations.navargs.primitives.arraylist

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavBackStackEntry
import com.ramcosta.composedestinations.navargs.DestinationsNavType
import com.ramcosta.composedestinations.navargs.primitives.DECODED_NULL
import com.ramcosta.composedestinations.navargs.primitives.ENCODED_NULL
import com.ramcosta.composedestinations.navargs.primitives.encodedComma

object DestinationsLongArrayListNavType : DestinationsNavType<ArrayList<Long>?>() {

    override fun put(bundle: Bundle, key: String, value: ArrayList<Long>?) {
        bundle.putLongArray(key, value?.let { list -> LongArray(list.size) { list[it] } })
    }

    override fun get(bundle: Bundle, key: String): ArrayList<Long>? {
        return bundle.getLongArray(key)?.toList()?.let { ArrayList(it) }
    }

    override fun parseValue(value: String): ArrayList<Long>? {
        return when (value) {
            DECODED_NULL -> null
            "[]" -> arrayListOf()
            else -> {
                val contentValue = value.subSequence(1, value.length - 1)

                if (contentValue.contains(encodedComma)) {
                    contentValue.split(encodedComma)
                } else {
                    contentValue.split(",")
                }.mapTo(ArrayList()) { LongType.parseValue(it) }
            }
        }
    }

    override fun serializeValue(value: ArrayList<Long>?): String {
        value ?: return ENCODED_NULL
        return "[${value.joinToString(",") { it.toString() }}]"
    }

    override fun get(navBackStackEntry: NavBackStackEntry, key: String): ArrayList<Long>? {
        return navBackStackEntry.arguments?.getLongArray(key).toArrayList()
    }

    override fun get(savedStateHandle: SavedStateHandle, key: String): ArrayList<Long>? {
        return savedStateHandle.get<LongArray?>(key).toArrayList()
    }

    private fun LongArray?.toArrayList(): ArrayList<Long>? {
        return this?.let { ArrayList(it.toList()) }
    }
}