package com.ramcosta.composedestinations.codegen.model

sealed class DestinationStyleType {
    class Animated(val type: Type, val requireOptInAnnotations: List<String>) : DestinationStyleType()

    class Dialog(val type: Type) : DestinationStyleType()

    object BottomSheet : DestinationStyleType()

    object Default : DestinationStyleType()
}