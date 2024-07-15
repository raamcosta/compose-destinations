package com.ramcosta.composedestinations.deeplinks

import androidx.navigation.NavDeepLink

actual fun navDeepLink(deepLinkBuilder: AndroidNavDeepLinkBuilder.() -> Unit): NavDeepLink? {
    return null
}