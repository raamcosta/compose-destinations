package com.ramcosta.composedestinations.codegen.model

sealed class DestinationStyleType {
    data class Animated(val type: Type, val requireOptInAnnotations: List<String>) : DestinationStyleType()

    data class Dialog(val type: Type) : DestinationStyleType()

    object BottomSheet : DestinationStyleType()

    object Default : DestinationStyleType()
}