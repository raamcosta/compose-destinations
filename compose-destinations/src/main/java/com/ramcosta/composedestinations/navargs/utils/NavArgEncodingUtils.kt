package com.ramcosta.composedestinations.navargs.utils

import android.net.Uri

fun encodeForRoute(arg: String): String {
    return Uri.encode(arg)
}