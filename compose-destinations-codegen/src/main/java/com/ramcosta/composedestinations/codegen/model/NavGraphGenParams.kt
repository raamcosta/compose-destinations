package com.ramcosta.composedestinations.codegen.model

import com.ramcosta.composedestinations.codegen.commons.toSnakeCase
import java.util.Locale

interface NavGraphGenParams {
    val sourceIds: List<String>
    val name: String
    val baseRoute: String
    val type: Importable
    val default: Boolean
    val isNavHostGraph: Boolean
    val defaultTransitions: Importable?
    val deepLinks: List<DeepLink>
    val navArgs: RawNavArgsClass?
    val parent: Importable?
    val isParentStart: Boolean?
}

data class RawNavGraphGenParams(
    override val type: Importable,
    override val default: Boolean,
    override val isNavHostGraph: Boolean,
    override val defaultTransitions: Importable?,
    override val deepLinks: List<DeepLink>,
    override val navArgs: RawNavArgsClass?,
    override val sourceIds: List<String>,
    override val parent: Importable? = null,
    override val isParentStart: Boolean? = null,
    private val routeOverride: String? = null,
): NavGraphGenParams {
    private var nameOverride: String? = null

    internal fun copyWithNameForRoute(newRoute: String): RawNavGraphGenParams {
        return copy(routeOverride = newRoute).apply {
            nameOverride = newRoute.replaceFirstChar { it.uppercase(Locale.US) } + "Graph"
        }
    }

    override val name: String get() = nameOverride ?: type.simpleName.let {
        if (it.endsWith("NavGraph")) {
            it.replace("NavGraph", "Graph")
        } else if (it.endsWith("Graph")) {
            it
        } else {
            it + "Graph"
        }
    }

    override val baseRoute: String by lazy(LazyThreadSafetyMode.NONE) {
        routeOverride ?: name.replace("(?i)graph".toRegex(), "").toSnakeCase()
    }
}
