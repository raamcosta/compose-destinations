package com.ramcosta.composedestinations.utils

import android.util.Base64
import java.nio.charset.Charset

internal fun String.base64ToByteArray(): ByteArray {
    return Base64.decode(
        toByteArray(Charset.defaultCharset()),
        Base64.URL_SAFE or Base64.NO_WRAP
    )
}

internal fun ByteArray.toBase64Str(): String {
    return Base64.encodeToString(this, Base64.URL_SAFE or Base64.NO_WRAP)
}