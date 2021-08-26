package com.ramcosta.composedestinations.annotation

@Target(AnnotationTarget.FUNCTION)
annotation class Destination(
    val route: String
)