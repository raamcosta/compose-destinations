package com.ramcosta.composedestinations.codegen.model

import com.ramcosta.composedestinations.codegen.commons.toSnakeCase
import java.util.*

data class RawNavGraphGenParams(
    val type: Importable,
    val default: Boolean,
    val isNavHostGraph: Boolean,
    val defaultTransitions: Importable?,
    private val routeOverride: String? = null,
    val parent: Importable? = null,
    val isParentStart: Boolean? = null,
) {
    private var nameOverride: String? = null

    internal fun copyWithNameForRoute(newRoute: String): RawNavGraphGenParams {
        return copy(routeOverride = newRoute).apply {
            nameOverride = newRoute.replaceFirstChar { it.uppercase(Locale.US) } + "NavGraph"
        }
    }

    val name: String get() = nameOverride ?: type.simpleName

    val route: String by lazy(LazyThreadSafetyMode.NONE) {
        routeOverride ?: name.replace("(?i)navgraph".toRegex(), "").toSnakeCase()
    }
}
