package com.ramcosta.composedestinations.codegen.model

data class NavGraphInfo(
    val start: Boolean,
    val isNavHostGraph: Boolean,
    val graphType: Importable
)