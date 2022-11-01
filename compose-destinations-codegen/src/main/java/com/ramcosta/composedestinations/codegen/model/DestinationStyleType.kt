package com.ramcosta.composedestinations.codegen.model

sealed class DestinationStyleType {
    data class Animated(val type: TypeInfo, val requireOptInAnnotations: List<Importable>) : DestinationStyleType()

    data class Dialog(val type: TypeInfo) : DestinationStyleType()

    object BottomSheet : DestinationStyleType()

    object Default : DestinationStyleType()

    object Runtime: DestinationStyleType()

    object Activity: DestinationStyleType()
}