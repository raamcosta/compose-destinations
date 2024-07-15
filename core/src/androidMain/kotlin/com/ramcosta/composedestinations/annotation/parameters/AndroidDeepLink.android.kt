@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package com.ramcosta.composedestinations.annotation.parameters

actual typealias AndroidDeepLink = DeepLink

/**
 * Can be used in [com.ramcosta.composedestinations.annotation.Destination] to add a deep link
 * connection to that destination.
 */
@Retention(AnnotationRetention.SOURCE)
annotation class DeepLink(
    val action: String = "",
    val mimeType: String = "",
    val uriPattern: String = ""
)
