package com.ramcosta.composedestinations.codegen.model

import com.ramcosta.composedestinations.codegen.commons.toSnakeCase

data class RawNavGraphGenParams(
    val type: ClassType,
    val default: Boolean,
    private val routeOverride: String? = null,
    val parent: ClassType? = null,
    val isParentStart: Boolean? = null,
) {

    val name: String = type.simpleName

    val route: String by lazy(LazyThreadSafetyMode.NONE) {
        routeOverride ?: name.replace("(?i)navgraph".toRegex(), "").toSnakeCase()
    }
}
