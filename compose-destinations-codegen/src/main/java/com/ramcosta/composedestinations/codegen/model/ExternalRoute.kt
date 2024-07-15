package com.ramcosta.composedestinations.codegen.model

sealed interface ExternalRoute {
    fun canUseOriginal(): Boolean

    val superType: TypeInfo
    val generatedType: Importable
    val navArgs: RawNavArgsClass?
    val requireOptInAnnotationTypes: List<Importable>
    val isStart: Boolean

    val additionalDeepLinks: List<DeepLink>

    data class Destination(
        override val superType: TypeInfo,
        override val isStart: Boolean,
        override val generatedType: Importable,
        override val navArgs: RawNavArgsClass?,
        override val requireOptInAnnotationTypes: List<Importable>,
        override val additionalDeepLinks: List<DeepLink>,
        val overriddenDestinationStyleType: DestinationStyleType?,
        val additionalComposableWrappers: List<Importable>
    ) : ExternalRoute {
        override fun canUseOriginal(): Boolean {
            return overriddenDestinationStyleType == null
                    && additionalDeepLinks.isEmpty()
                    && additionalComposableWrappers.isEmpty()
        }
    }

    data class NavGraph(
        override val superType: TypeInfo,
        override val isStart: Boolean,
        override val generatedType: Importable,
        override val navArgs: RawNavArgsClass?,
        override val additionalDeepLinks: List<DeepLink>,
        override val requireOptInAnnotationTypes: List<Importable>,
        val overriddenDefaultTransitions: OverrideDefaultTransitions,
    ) : ExternalRoute {

        override fun canUseOriginal(): Boolean {
            return overriddenDefaultTransitions is OverrideDefaultTransitions.NoOverride
                    && additionalDeepLinks.isEmpty()
        }

        sealed interface OverrideDefaultTransitions {
            data object NoOverride: OverrideDefaultTransitions
            data class Override(val importable: Importable?): OverrideDefaultTransitions
        }
    }

}