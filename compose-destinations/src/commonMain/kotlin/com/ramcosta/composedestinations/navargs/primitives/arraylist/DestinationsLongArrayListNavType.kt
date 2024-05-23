package com.ramcosta.composedestinations.navargs.primitives.arraylist

import androidx.core.bundle.Bundle
import androidx.lifecycle.SavedStateHandle
import com.ramcosta.composedestinations.navargs.DestinationsNavType
import com.ramcosta.composedestinations.navargs.primitives.DECODED_NULL
import com.ramcosta.composedestinations.navargs.primitives.ENCODED_NULL
import com.ramcosta.composedestinations.navargs.primitives.encodedComma

object DestinationsLongArrayListNavType : DestinationsNavType<ArrayList<Long>?>() {

    override fun put(bundle: Bundle, key: String, value: ArrayList<Long>?) {
        bundle.putLongArray(key, value.toArray())
    }

    override fun get(bundle: Bundle, key: String): ArrayList<Long>? {
        return bundle.getLongArray(key).toArrayList()
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

    override fun get(savedStateHandle: SavedStateHandle, key: String): ArrayList<Long>? {
        return savedStateHandle.get<LongArray?>(key).toArrayList()
    }

    override fun put(savedStateHandle: SavedStateHandle, key: String, value: ArrayList<Long>?) {
        savedStateHandle[key] = value.toArray()
    }

    private fun ArrayList<Long>?.toArray(): LongArray? {
        return this?.let { list -> LongArray(list.size) { list[it] } }
    }

    private fun LongArray?.toArrayList(): ArrayList<Long>? {
        return this?.let { ArrayList(it.toList()) }
    }
}