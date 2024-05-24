package com.ramcosta.composedestinations.navargs.primitives.arraylist

import androidx.core.bundle.Bundle
import androidx.lifecycle.SavedStateHandle
import com.ramcosta.composedestinations.navargs.DestinationsNavType
import com.ramcosta.composedestinations.navargs.primitives.DECODED_NULL
import com.ramcosta.composedestinations.navargs.primitives.ENCODED_NULL
import com.ramcosta.composedestinations.navargs.primitives.encodedComma

object DestinationsIntArrayListNavType : DestinationsNavType<ArrayList<Int>?>() {

    override fun put(bundle: Bundle, key: String, value: ArrayList<Int>?) {
        bundle.putIntegerArrayList(key, value)
    }

    override fun get(bundle: Bundle, key: String): ArrayList<Int>? {
        return bundle.getIntegerArrayList(key)
    }

    override fun parseValue(value: String): ArrayList<Int>? {
        return when (value) {
            DECODED_NULL -> null
            "[]" -> arrayListOf()
            else -> {
                val contentValue = value.subSequence(1, value.length - 1)
                if (contentValue.contains(encodedComma)) {
                    contentValue.split(encodedComma)
                } else {
                    contentValue.split(",")
                }.mapTo(ArrayList()) { IntType.parseValue(it) }
            }
        }
    }

    override fun serializeValue(value: ArrayList<Int>?): String {
        value ?: return ENCODED_NULL
        return "[${value.joinToString(",") { it.toString() }}]"
    }

    override fun get(savedStateHandle: SavedStateHandle, key: String): ArrayList<Int>? {
        return savedStateHandle[key]
    }

    override fun put(savedStateHandle: SavedStateHandle, key: String, value: ArrayList<Int>?) {
        savedStateHandle[key] = value
    }
}