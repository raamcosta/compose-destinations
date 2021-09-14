package com.ramcosta.composedestinations.annotation

/**
 * Can be used in [Destination] to describe a deep link
 * connection to that destination.
 */
@Retention(AnnotationRetention.SOURCE)
annotation class DeepLink(
    val action: String = "",
    val mimeType: String = "",
    val uriPattern: String = ""
)