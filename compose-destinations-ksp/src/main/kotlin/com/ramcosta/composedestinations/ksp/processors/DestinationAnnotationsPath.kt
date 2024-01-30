package com.ramcosta.composedestinations.ksp.processors

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSFunctionDeclaration

data class DestinationAnnotationsPath(
    val annotations: List<KSAnnotation>,
    val function: KSFunctionDeclaration
)