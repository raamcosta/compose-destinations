package com.ramcosta.composedestinations.codegen.model

import com.ramcosta.composedestinations.codegen.commons.IllegalDestinationsSetup

sealed interface DefaultValue {

    data class Error(val throwable: Throwable) : DefaultValue

    data object NonExistent: DefaultValue

    data class Available(
        val code: String,
        val imports: List<String> = emptyList()
    ) : DefaultValue
}

fun DefaultValue.unwrapAvailable(): DefaultValue.Available? = when (this) {
    is DefaultValue.Available -> this
    is DefaultValue.Error -> throw IllegalDestinationsSetup("Error reading default value", throwable)
    is DefaultValue.NonExistent -> null
}
