package com.ramcosta.composedestinations.navargs.utils

import platform.Foundation.NSCharacterSet
import platform.Foundation.NSString
import platform.Foundation.URLQueryAllowedCharacterSet
import platform.Foundation.stringByAddingPercentEncodingWithAllowedCharacters

actual fun encodeForRoute(arg: String): String {
    @Suppress("CAST_NEVER_SUCCEEDS") // it always succeeds
    val encodedString = (arg as NSString).stringByAddingPercentEncodingWithAllowedCharacters(
        NSCharacterSet.URLQueryAllowedCharacterSet
    )!!
    return encodedString
}
