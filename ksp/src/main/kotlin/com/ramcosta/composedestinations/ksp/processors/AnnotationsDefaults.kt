package com.ramcosta.composedestinations.ksp.processors

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSType
import com.ramcosta.composedestinations.codegen.commons.DESTINATION_ANNOTATION_DEFAULT_ROUTE_PLACEHOLDER
import com.ramcosta.composedestinations.codegen.commons.NAV_GRAPH_ANNOTATION_DEFAULT_NAME

lateinit var defaultsMap: Map<String, Map<String, Any>>

fun Resolver.initializeDefaultsMap() {
    val kNothingKsType = getClassDeclarationByName("kotlin.Nothing")!!.asStarProjectedType()
    val publicVisibilityKsType = getClassDeclarationByName(
        "com.ramcosta.composedestinations.annotation.parameters.CodeGenVisibility.PUBLIC"
    )!!.asStarProjectedType()

    defaultsMap = mapOf(
        "com.ramcosta.composedestinations.annotation.Destination" to mapOf(
            "route" to DESTINATION_ANNOTATION_DEFAULT_ROUTE_PLACEHOLDER,
            "start" to false,
            "navArgs" to kNothingKsType,
            "deepLinks" to arrayListOf<KSAnnotation>(),
            "style" to getClassDeclarationByName("com.ramcosta.composedestinations.spec.DestinationStyle.Default")!!.asStarProjectedType(),
            "wrappers" to arrayListOf<KSType>(),
            "visibility" to publicVisibilityKsType
        ),
        "com.ramcosta.composedestinations.annotation.NavGraph" to mapOf(
            "route" to NAV_GRAPH_ANNOTATION_DEFAULT_NAME,
            "start" to false,
            "navArgs" to kNothingKsType,
            "deepLinks" to arrayListOf<KSAnnotation>(),
            "defaultTransitions" to kNothingKsType,
            "visibility" to publicVisibilityKsType
        ),
        "com.ramcosta.composedestinations.annotation.NavHostGraph" to mapOf(
            "defaultTransitions" to getClassDeclarationByName("com.ramcosta.composedestinations.animations.defaults.NoTransitions")!!.asStarProjectedType(),
            "route" to NAV_GRAPH_ANNOTATION_DEFAULT_NAME,
            "visibility" to publicVisibilityKsType
        ),
        "com.ramcosta.composedestinations.annotation.ExternalDestination" to mapOf(
            "start" to false,
            "deepLinks" to arrayListOf<KSAnnotation>(),
            "style" to kNothingKsType,
            "wrappers" to arrayListOf<KSType>(),
        ),
        "com.ramcosta.composedestinations.annotation.ExternalNavGraph" to mapOf(
            "start" to false,
            "deepLinks" to arrayListOf<KSAnnotation>(),
            "defaultTransitions" to getClassDeclarationByName("com.ramcosta.composedestinations.annotation.ExternalNavGraph.Companion.NoOverride")!!.asStarProjectedType(),
        ),
        "com.ramcosta.composedestinations.annotation.parameters.DeepLink" to mapOf(
            "action" to "",
            "mimeType" to "",
            "uriPattern" to "",
        ),
        "com.ramcosta.composedestinations.annotation.parameters.AndroidDeepLink" to mapOf(
            "action" to "",
            "mimeType" to "",
            "uriPattern" to "",
        )
    )
}