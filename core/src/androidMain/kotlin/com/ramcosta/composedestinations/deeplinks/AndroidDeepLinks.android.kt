package com.ramcosta.composedestinations.deeplinks

import androidx.navigation.NavDeepLink
import androidx.navigation.NavDeepLinkDslBuilder

actual fun navDeepLink(deepLinkBuilder: AndroidNavDeepLinkBuilder.() -> Unit): NavDeepLink? {
    return androidx.navigation.navDeepLink {
        deepLinkBuilder(AndroidNavDeepLinkBuilderImpl(this))
    }
}

private class AndroidNavDeepLinkBuilderImpl(
    private val navDeepLinkDslBuilder: NavDeepLinkDslBuilder
): AndroidNavDeepLinkBuilder {
    override var uriPattern: String?
        get() = navDeepLinkDslBuilder.uriPattern
        set(value) { navDeepLinkDslBuilder.uriPattern = value }

    override var mimeType: String?
        get() = navDeepLinkDslBuilder.mimeType
        set(value) { navDeepLinkDslBuilder.mimeType = value }

    override var action: String?
        get() = navDeepLinkDslBuilder.action
        set(value) { navDeepLinkDslBuilder.action = value }
}
