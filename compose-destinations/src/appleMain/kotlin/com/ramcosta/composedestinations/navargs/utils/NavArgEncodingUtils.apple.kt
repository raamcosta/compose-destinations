package com.ramcosta.composedestinations.navargs.utils

import com.ramcosta.composedestinations.navargs.utils.Base64.decodeFromBase64
import com.ramcosta.composedestinations.navargs.utils.Base64.encodeToBase64
import platform.Foundation.NSCharacterSet
import platform.Foundation.NSString
import platform.Foundation.URLQueryAllowedCharacterSet
import platform.Foundation.stringByAddingPercentEncodingWithAllowedCharacters

actual fun encodeForRoute(arg: String): String {
    @Suppress("CAST_NEVER_SUCCEEDS")
    val encodedString = (arg as NSString).stringByAddingPercentEncodingWithAllowedCharacters(
        NSCharacterSet.URLQueryAllowedCharacterSet
    )!!
    return encodedString
}

actual fun String.base64ToByteArray(): ByteArray {
    return decodeFromBase64()
}

actual fun ByteArray.toBase64Str(): String {
    return encodeToBase64()
}
