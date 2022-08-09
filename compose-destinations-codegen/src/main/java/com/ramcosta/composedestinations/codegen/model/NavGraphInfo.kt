package com.ramcosta.composedestinations.codegen.model

sealed interface NavGraphInfo {

    val start: Boolean
    val isDefault: Boolean
    val isTopLevelGraph: Boolean

    data class Legacy(
        override val start: Boolean,
        val navGraphRoute: String
    ): NavGraphInfo {
        override val isDefault: Boolean
            get() = !start && navGraphRoute == "root"

        override val isTopLevelGraph: Boolean
            get() = navGraphRoute == "root"
    }

    data class AnnotatedSource(
        override val start: Boolean,
        override val isTopLevelGraph: Boolean,
        val graphType: Importable
    ): NavGraphInfo {
        override val isDefault = false
    }
}