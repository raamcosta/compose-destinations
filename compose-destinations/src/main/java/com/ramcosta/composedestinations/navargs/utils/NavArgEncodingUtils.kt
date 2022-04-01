package com.ramcosta.composedestinations.navargs.utils

import android.net.Uri
import android.util.Base64
import java.nio.charset.Charset

fun encodeForRoute(arg: String): String {
    return Uri.encode(arg)
}

internal fun String.base64ToByteArray(): ByteArray {
    return Base64.decode(
        toByteArray(Charset.defaultCharset()),
        Base64.URL_SAFE or Base64.NO_WRAP
    )
}

internal fun ByteArray.toBase64Str(): String {
    return Base64.encodeToString(this, Base64.URL_SAFE or Base64.NO_WRAP)
}