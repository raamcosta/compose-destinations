package com.ramcosta.composedestinations.codegen.model

data class RawNavArgsClass(
    val parameters: List<Parameter>,
    val visibility: Visibility,
    val type: Importable,
    // useful only for nav graph args cases
    val extraStartRouteArgs: RawNavArgsClass? = null
)
