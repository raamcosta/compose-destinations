package com.ramcosta.composedestinations.codegen.model

sealed class DestinationStyleType {
    data class Animated(val importable: Importable, val requireOptInAnnotations: List<Importable>) : DestinationStyleType()

    data class Dialog(val importable: Importable) : DestinationStyleType()

    object BottomSheet : DestinationStyleType()

    object Default : DestinationStyleType()

    object Runtime: DestinationStyleType()

    object Activity: DestinationStyleType()
}