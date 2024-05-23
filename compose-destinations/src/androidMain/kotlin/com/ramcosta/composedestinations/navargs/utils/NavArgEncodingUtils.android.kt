package com.ramcosta.composedestinations.navargs.utils

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Build
import android.util.Base64
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

actual fun encodeForRoute(arg: String): String {
    return if (!isRunningOnUnitTests) {
        Uri.encode(arg)
    } else {
        URLEncoder.encode(arg, StandardCharsets.UTF_8.toString())
    }
}

@SuppressLint("NewApi")
actual fun String.base64ToByteArray(): ByteArray {
    return if (shouldUseJavaUtil) {
        java.util.Base64.getUrlDecoder().decode(toByteArray(StandardCharsets.UTF_8))
    } else {
        Base64.decode(toByteArray(StandardCharsets.UTF_8), Base64.URL_SAFE or Base64.NO_WRAP)
    }
}

@SuppressLint("NewApi")
actual fun ByteArray.toBase64Str(): String {
    return if (shouldUseJavaUtil) {
        java.util.Base64.getUrlEncoder().encodeToString(this)
    } else {
        Base64.encodeToString(this, Base64.URL_SAFE or Base64.NO_WRAP)
    }
}

// Both encode/decode from java util and android util seem to
// do exactly the same.
// android.util one doesn't work on Unit tests and java.util doesn't work
// on older devices.
// Also, on unit tests SDK_INT is 0, but we're running on the developer's machine
// So we can still use java.util in that case
@SuppressLint("ObsoleteSdkInt")
private val isRunningOnUnitTests = Build.VERSION.SDK_INT == 0

private val shouldUseJavaUtil =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O || isRunningOnUnitTests

