package com.ramcosta.composedestinations.ksp.processors

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType

data class DestinationAnnotationsPath(
    val annotations: List<KSAnnotation>,
    val function: KSFunctionDeclaration
) {
    val functionAnnotationsResolved: Sequence<KSType> by lazy {
        function.annotations.map { it.annotationType.resolve() }
    }

    val annotationsOfAnnotationsResolved: List<KSAnnotation> by lazy {
        annotations.flatMap { it.annotationType.resolve().declaration.annotations }
    }
}