package com.ramcosta.composedestinations.navargs.utils

expect fun encodeForRoute(arg: String): String

expect fun String.base64ToByteArray(): ByteArray

expect fun ByteArray.toBase64Str(): String
