package com.ramcosta.composedestinations.navargs.primitives.arraylist

import androidx.core.bundle.Bundle
import androidx.lifecycle.SavedStateHandle
import com.ramcosta.composedestinations.navargs.DestinationsNavType
import com.ramcosta.composedestinations.navargs.primitives.DECODED_NULL
import com.ramcosta.composedestinations.navargs.primitives.ENCODED_NULL
import com.ramcosta.composedestinations.navargs.primitives.encodedComma

object DestinationsFloatArrayListNavType : DestinationsNavType<ArrayList<Float>?>() {

    override fun put(bundle: Bundle, key: String, value: ArrayList<Float>?) {
        bundle.putFloatArray(key, value.toArray())
    }

    override fun get(bundle: Bundle, key: String): ArrayList<Float>? {
        return bundle.getFloatArray(key).toArrayList()
    }

    override fun parseValue(value: String): ArrayList<Float>? {
        return when (value) {
            DECODED_NULL -> null
            "[]" -> arrayListOf()
            else -> {
                val contentValue = value.subSequence(1, value.length - 1)
                if (contentValue.contains(encodedComma)) {
                    contentValue.split(encodedComma)
                } else {
                    contentValue.split(",")
                }.mapTo(ArrayList()) { FloatType.parseValue(it) }
            }
        }
    }

    override fun serializeValue(value: ArrayList<Float>?): String {
        value ?: return ENCODED_NULL
        return "[${value.joinToString(",") { it.toString() }}]"
    }

    override fun get(savedStateHandle: SavedStateHandle, key: String): ArrayList<Float>? {
        return savedStateHandle.get<FloatArray?>(key).toArrayList()
    }

    override fun put(savedStateHandle: SavedStateHandle, key: String, value: ArrayList<Float>?) {
        savedStateHandle[key] = value.toArray()
    }

    private fun ArrayList<Float>?.toArray(): FloatArray? {
        return this?.let { list -> FloatArray(list.size) { list[it] } }
    }

    private fun FloatArray?.toArrayList(): ArrayList<Float>? {
        return this?.let { ArrayList(it.toList()) }
    }
}