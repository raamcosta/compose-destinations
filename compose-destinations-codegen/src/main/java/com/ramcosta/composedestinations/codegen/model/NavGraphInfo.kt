package com.ramcosta.composedestinations.codegen.model

sealed interface NavGraphInfo {

    val start: Boolean
    val isDefault: Boolean

    data class Legacy(
        override val start: Boolean,
        val navGraphRoute: String
    ): NavGraphInfo {
        override val isDefault: Boolean
            get() = !start && navGraphRoute == "root"
    }

    data class AnnotatedSource(
        override val start: Boolean,
        val graphType: ClassType
    ): NavGraphInfo {
        override val isDefault = false
    }
}