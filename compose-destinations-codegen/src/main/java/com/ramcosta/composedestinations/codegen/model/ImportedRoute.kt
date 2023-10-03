package com.ramcosta.composedestinations.codegen.model

sealed interface IncludedRoute {
    val generatedType: Importable
    val navArgs: RawNavArgsClass?
    val requireOptInAnnotationTypes: List<Importable>
    val isStart: Boolean

    val additionalDeepLinks: List<DeepLink>

    class Destination(
        override val isStart: Boolean,
        override val generatedType: Importable,
        override val navArgs: RawNavArgsClass?,
        override val requireOptInAnnotationTypes: List<Importable>,
        override val additionalDeepLinks: List<DeepLink>,
        val overriddenDestinationStyleType: DestinationStyleType?,
        val additionalComposableWrappers: List<Importable>
    ) : IncludedRoute

    class NavGraph(
        override val isStart: Boolean,
        override val generatedType: Importable,
        override val navArgs: RawNavArgsClass?,
        override val additionalDeepLinks: List<DeepLink>,
        override val requireOptInAnnotationTypes: List<Importable>,
        val overriddenDefaultTransitions: OverrideDefaultTransitions,
    ) : IncludedRoute {

        sealed interface OverrideDefaultTransitions {
            data object NoOverride: OverrideDefaultTransitions
            class Override(val importable: Importable?): OverrideDefaultTransitions
        }
    }

}