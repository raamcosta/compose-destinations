@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package com.ramcosta.composedestinations.annotation.parameters

actual typealias AndroidDeepLink = DoNothing

@Retention(AnnotationRetention.SOURCE)
annotation class DoNothing(
    val action: String = "",
    val mimeType: String = "",
    val uriPattern: String = "",
)