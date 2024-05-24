package com.ramcosta.composedestinations.navargs.primitives.arraylist

import androidx.core.bundle.Bundle
import androidx.lifecycle.SavedStateHandle
import com.ramcosta.composedestinations.navargs.DestinationsNavType
import com.ramcosta.composedestinations.navargs.primitives.DECODED_NULL
import com.ramcosta.composedestinations.navargs.primitives.ENCODED_NULL
import com.ramcosta.composedestinations.navargs.primitives.encodedComma

class DestinationsEnumArrayListNavType<E : Enum<*>>(
    private val converter: (String) -> E
) : DestinationsNavType<ArrayList<E>?>() {

    override fun put(bundle: Bundle, key: String, value: ArrayList<E>?) {
        bundle.putString(key, serializeValue(value))
    }

    override fun get(bundle: Bundle, key: String): ArrayList<E>? {
        return bundle.getString(key)?.let { parseValue(it) }
    }

    override fun parseValue(value: String): ArrayList<E>? {
        if (value == DECODED_NULL) return null

        if (value == "[]") return arrayListOf()

        val contentValue = value.subSequence(1, value.length - 1)
        val splits = if (contentValue.contains(encodedComma)) {
            contentValue.split(encodedComma)
        } else {
            contentValue.split(",")
        }

        return splits.mapTo(arrayListOf(), converter)
    }

    override fun serializeValue(value: ArrayList<E>?): String {
        if (value == null) return ENCODED_NULL
        return "[${value.joinToString(",") { it.name }}]"
    }

    override fun get(savedStateHandle: SavedStateHandle, key: String): ArrayList<E>? {
        return savedStateHandle.get<String?>(key)?.let { parseValue(it) }
    }

    override fun put(savedStateHandle: SavedStateHandle, key: String, value: ArrayList<E>?) {
        savedStateHandle[key] = value?.let { serializeValue(it) }
    }
}
