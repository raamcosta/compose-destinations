package com.ramcosta.composedestinations.ksp.processors

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.ramcosta.composedestinations.codegen.commons.IllegalDestinationsSetup
import com.ramcosta.composedestinations.codegen.commons.NAV_GRAPH_ANNOTATION_DEFAULT_NAME
import com.ramcosta.composedestinations.codegen.commons.NAV_GRAPH_ANNOTATION_QUALIFIED
import com.ramcosta.composedestinations.codegen.model.ClassType
import com.ramcosta.composedestinations.codegen.model.RawNavGraphGenParams

class KspToCodeGenNavGraphsMapper {

    fun map(navGraphAnnotations: Sequence<KSClassDeclaration>): List<RawNavGraphGenParams> {
        return navGraphAnnotations.map { it.mapToRawNavGraphGenParams() }.toList()
    }

    private fun KSClassDeclaration.mapToRawNavGraphGenParams(): RawNavGraphGenParams {
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

        val navGraphAnnotation = annotations
            .first { it.shortName.asString() == "NavGraph" }
        val navGraphAnnotationNameArg = navGraphAnnotation
            .arguments.find { it.name?.asString() == "route" }!!.value as String
        val navGraphAnnotationDefaultArg = navGraphAnnotation
            .arguments.find { it.name?.asString() == "default" }!!.value as Boolean

        var parentGraphAnnotationResolved: KSType? = null
        val parentGraphAnnotation = annotations.firstOrNull { annotationOfAnnotation ->
            if (annotationOfAnnotation.shortName.asString() == "NavGraph") return@firstOrNull false

            val resolved = annotationOfAnnotation.annotationType.resolve()
            resolved.declaration.annotations.any {
                if (it.shortName.asString() != "NavGraph") {
                    return@any false
                }

                it.annotationType.resolve().declaration.qualifiedName?.asString() == NAV_GRAPH_ANNOTATION_QUALIFIED
            }.also {
                if (it) parentGraphAnnotationResolved = resolved
            }
        }

        val isParentStart = parentGraphAnnotation?.arguments?.first()?.value as? Boolean?
        val parent = parentGraphAnnotationResolved?.let {
            ClassType(
                it.declaration.simpleName.asString(),
                it.declaration.qualifiedName!!.asString()
            )
        }

        return RawNavGraphGenParams(
            routeOverride = if (navGraphAnnotationNameArg == NAV_GRAPH_ANNOTATION_DEFAULT_NAME) null else navGraphAnnotationNameArg,
            default = navGraphAnnotationDefaultArg,
            type = ClassType(this.simpleName.asString(), this.qualifiedName!!.asString()),
            parent = parent,
            isParentStart = isParentStart
        )
    }
}
