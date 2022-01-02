package com.ramcosta.composedestinations.navargs

import android.net.Uri
import android.os.Bundle
import androidx.navigation.NavType

object DestinationsStringNavType : NavType<String?>(true) {

    private const val ENCODED_EMPTY_STRING = "%02%03"
    private val DECODED_EMPTY_STRING: String = Uri.decode(ENCODED_EMPTY_STRING)

    override fun put(bundle: Bundle, key: String, value: String?) {
        StringType.put(bundle, key, value)
    }

    override fun get(bundle: Bundle, key: String): String? {
        return StringType[bundle, key]
    }

    override fun parseValue(value: String): String {
        return if (value == DECODED_EMPTY_STRING) {
            ""
        } else {
            value
        }
    }

    fun encodeForRoute(value: String?, isMandatoryArg: Boolean): String? {
        if (value == null) {
            return null
        }

        if (value.isEmpty()) {
            return ENCODED_EMPTY_STRING
        }

        return if (!isMandatoryArg) {
            // Non mandatory parameters are decoded twice internally for some reason
            // So, if we want strings like "%25" to be parsed from the route correctly,
            // we also need to encode these twice.
            Uri.encode(Uri.encode(value))
        } else {
            Uri.encode(value)
        }
    }
}