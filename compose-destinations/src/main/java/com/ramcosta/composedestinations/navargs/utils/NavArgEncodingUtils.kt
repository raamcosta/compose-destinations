package com.ramcosta.composedestinations.navargs.utils

import android.net.Uri

fun encodeForRoute(arg: String, isMandatoryArg: Boolean): String {
    return if (isMandatoryArg) {
        Uri.encode(arg)
    } else {
        // Non mandatory parameters are decoded twice internally due to a bug:
        // https://issuetracker.google.com/issues/210711399
        // So for now, if we want strings like "%25" to be parsed from the route correctly,
        // we also need to encode these twice.
        Uri.encode(Uri.encode(arg))
    }
}