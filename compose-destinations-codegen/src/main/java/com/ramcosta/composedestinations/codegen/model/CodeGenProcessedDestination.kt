package com.ramcosta.composedestinations.codegen.model

import com.ramcosta.composedestinations.codegen.commons.recursiveRequireOptInAnnotations
import com.ramcosta.composedestinations.codegen.writers.destinationsPackageName

internal data class CodeGenProcessedDestination(
    val navArgs: List<Parameter>,
    val destinationGeneratingParams: DestinationGeneratingParams
) : DestinationGeneratingParams by destinationGeneratingParams {

    val destinationImportable = Importable(
        simpleName = name,
        qualifiedName = "$destinationsPackageName.$name",
    )

    val optInAnnotations: List<OptInAnnotation> = gatherOptInAnnotations()
    val navArgsDataClassImportable: Importable? = navArgsDataClassImportable()
    val navArgsClass: RawNavArgsClass? = navArgsClass()

    private fun navArgsClass() = navArgsDataClassImportable?.let {
        RawNavArgsClass(
            parameters = navArgs,
            visibility = destinationNavArgsClass?.visibility ?: visibility,
            type = it
        )
    }

    private fun navArgsDataClassImportable(): Importable? {
        return destinationNavArgsClass?.type
            ?: if (navArgs.isEmpty()) {
                null
            } else {
                Importable(
                    "${name}NavArgs",
                    "${destinationsPackageName}.${name}NavArgs"
                )
            }
    }

    private fun gatherOptInAnnotations(): List<OptInAnnotation> {
        val optInByAnnotation = destinationGeneratingParams.requireOptInAnnotationTypes.associateWithTo(mutableMapOf()) { false }

        destinationGeneratingParams.parameters.forEach { param ->
            optInByAnnotation.putAll(
                param.type.recursiveRequireOptInAnnotations().associateWith { requireOptInType ->
                    // if the destination itself doesn't need this annotation, then it was opted in
                    !destinationGeneratingParams.requireOptInAnnotationTypes.contains(requireOptInType)
                }
            )
        }

        val destinationStyleType = destinationGeneratingParams.destinationStyleType
        if (destinationStyleType is DestinationStyleType.Animated) {
            optInByAnnotation.putAll(destinationStyleType.requireOptInAnnotations.associateWithTo(mutableMapOf()) { false })
        }

        return optInByAnnotation.map { OptInAnnotation(it.key, it.value) }
    }

    class OptInAnnotation(
        val importable: Importable,
        val isOptedIn: Boolean,
    )
}