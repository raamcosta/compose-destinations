package com.ramcosta.composedestinations.annotation.internal

@RequiresOptIn(message = "This API is internal to Compose Destinations library. Do NOT use it!")
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
annotation class InternalDestinationsApi
