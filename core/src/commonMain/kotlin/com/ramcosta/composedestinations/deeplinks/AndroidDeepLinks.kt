package com.ramcosta.composedestinations.deeplinks

import androidx.navigation.NavDeepLink

interface AndroidNavDeepLinkBuilder {
    var uriPattern: String?
    var mimeType: String?
    var action: String?
}

/**
 * Returns a [NavDeepLink] on android target and null on all others.
 */
expect fun navDeepLink(deepLinkBuilder: AndroidNavDeepLinkBuilder.() -> Unit): NavDeepLink?