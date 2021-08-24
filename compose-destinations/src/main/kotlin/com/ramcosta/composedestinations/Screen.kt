package com.ramcosta.composedestinations

@Target(AnnotationTarget.FUNCTION)
annotation class Screen(
    val route: String = ""
)