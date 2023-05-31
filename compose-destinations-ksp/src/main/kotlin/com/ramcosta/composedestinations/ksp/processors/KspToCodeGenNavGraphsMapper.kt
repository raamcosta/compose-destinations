package com.ramcosta.composedestinations.ksp.processors

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.ramcosta.composedestinations.codegen.commons.IllegalDestinationsSetup
import com.ramcosta.composedestinations.codegen.commons.NAV_GRAPH_ANNOTATION
import com.ramcosta.composedestinations.codegen.commons.NAV_GRAPH_ANNOTATION_DEFAULT_NAME
import com.ramcosta.composedestinations.codegen.commons.NAV_GRAPH_ANNOTATION_QUALIFIED
import com.ramcosta.composedestinations.codegen.commons.NAV_HOST_GRAPH_ANNOTATION
import com.ramcosta.composedestinations.codegen.commons.NAV_HOST_GRAPH_ANNOTATION_QUALIFIED
import com.ramcosta.composedestinations.codegen.model.Importable
import com.ramcosta.composedestinations.codegen.model.RawNavGraphGenParams
import com.ramcosta.composedestinations.ksp.commons.findActualClassDeclaration
import com.ramcosta.composedestinations.ksp.commons.findArgumentValue
import com.ramcosta.composedestinations.ksp.commons.isNothing
import com.ramcosta.composedestinations.ksp.commons.toImportable

class KspToCodeGenNavGraphsMapper {

    fun map(
        navGraphAnnotations: Sequence<KSClassDeclaration>,
        navHostGraphAnnotations: Sequence<KSClassDeclaration>
    ): List<RawNavGraphGenParams> {
        return navGraphAnnotations.map { it.mapToRawNavGraphGenParams(false) }.toList() +
                navHostGraphAnnotations.map { it.mapToRawNavGraphGenParams(true) }
    }

    private fun KSClassDeclaration.mapToRawNavGraphGenParams(isNavHostGraph: Boolean): RawNavGraphGenParams {
        if (classKind != KSPClassKind.ANNOTATION_CLASS) {
            throw IllegalDestinationsSetup("Classes annotated with `@NavGraph` must be annotation classes!")
        }

       primaryConstructor?.parameters?.firstOrNull {
            it.name?.asString() == "start" &&
                    it.type.toString() == "Boolean" &&
                    it.hasDefault
        }
            ?: throw IllegalDestinationsSetup("Classes annotated with `@NavGraph` must contain " +
                    "a single parameter like: `val start: Boolean = false`!")

        val navGraphAnnotation = if (isNavHostGraph) {
            annotations.first {
                it.shortName.asString() == NAV_HOST_GRAPH_ANNOTATION
            }
        } else {
             annotations.first {
                 it.shortName.asString() == NAV_GRAPH_ANNOTATION
            }
        }

        val navGraphAnnotationNameArg = navGraphAnnotation
            .findArgumentValue<String>("route")
        val navGraphAnnotationDefaultArg = navGraphAnnotation
            .findArgumentValue<Boolean>("default")!!
        val navGraphDefaultTransitions = navGraphAnnotation
            .findArgumentValue<KSType>("defaultTransitions")
            ?.findActualClassDeclaration()
            ?.takeIf { !it.isNothing }
            ?.toImportable()

        if (isNavHostGraph && navGraphDefaultTransitions == null) {
            throw IllegalDestinationsSetup("A $NAV_HOST_GRAPH_ANNOTATION needs a non Nothing::class as defaultTransitions! " +
                    "Use `NoTransitions` if you wish to have no animations as default for this nav graph ${simpleName.asString()}")
        }

        var parentGraphAnnotationResolved: KSType? = null
        val parentGraphAnnotation = annotations.firstOrNull { annotationOfAnnotation ->
            if (annotationOfAnnotation.shortName.asString() == NAV_GRAPH_ANNOTATION) return@firstOrNull false

            val resolved = annotationOfAnnotation.annotationType.resolve()
            resolved.declaration.annotations.any {
                val annotationShortName = it.shortName.asString()
                if (annotationShortName != NAV_GRAPH_ANNOTATION && annotationShortName != NAV_HOST_GRAPH_ANNOTATION) {
                    return@any false
                }

                val annotationQualifiedName = it.annotationType.resolve().declaration.qualifiedName?.asString()
                annotationQualifiedName == NAV_GRAPH_ANNOTATION_QUALIFIED
                        || annotationQualifiedName == NAV_HOST_GRAPH_ANNOTATION_QUALIFIED
            }.also {
                if (it) parentGraphAnnotationResolved = resolved
            }
        }

        val isParentStart = parentGraphAnnotation?.arguments?.first()?.value as? Boolean?
        val parent = parentGraphAnnotationResolved?.let {
            Importable(
                it.declaration.simpleName.asString(),
                it.declaration.qualifiedName!!.asString()
            )
        }

        if (isNavHostGraph && parent != null) {
            throw IllegalDestinationsSetup("NavHostGraph's cannot have a parent navigation graph! (${simpleName.asString()}, parent = ${parent.simpleName})")
        }

        return RawNavGraphGenParams(
            routeOverride = if (navGraphAnnotationNameArg == NAV_GRAPH_ANNOTATION_DEFAULT_NAME) null else navGraphAnnotationNameArg,
            default = navGraphAnnotationDefaultArg,
            isNavHostGraph = isNavHostGraph,
            defaultTransitions = navGraphDefaultTransitions,
            type = Importable(this.simpleName.asString(), this.qualifiedName!!.asString()),
            parent = parent,
            isParentStart = isParentStart
        )
    }
}
