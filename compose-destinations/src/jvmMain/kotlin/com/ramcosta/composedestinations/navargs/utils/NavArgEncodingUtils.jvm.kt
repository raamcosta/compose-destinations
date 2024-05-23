package com.ramcosta.composedestinations.navargs.utils

import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Base64

actual fun encodeForRoute(arg: String): String {
    return URLEncoder.encode(arg, StandardCharsets.UTF_8.toString())
}

actual fun String.base64ToByteArray(): ByteArray {
    return Base64.getUrlDecoder().decode(toByteArray(StandardCharsets.UTF_8))
}

actual fun ByteArray.toBase64Str(): String {
    return Base64.getUrlEncoder().encodeToString(this)
}