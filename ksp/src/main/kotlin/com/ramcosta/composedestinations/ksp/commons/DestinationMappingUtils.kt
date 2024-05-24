package com.ramcosta.composedestinations.ksp.commons

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.ramcosta.composedestinations.codegen.commons.CORE_PACKAGE_NAME
import com.ramcosta.composedestinations.codegen.commons.DESTINATION_ANNOTATION_STYLE_ARGUMENT
import com.ramcosta.composedestinations.codegen.commons.DESTINATION_ANNOTATION_WRAPPERS_ARGUMENT
import com.ramcosta.composedestinations.codegen.commons.IllegalDestinationsSetup
import com.ramcosta.composedestinations.codegen.model.DestinationStyleType
import com.ramcosta.composedestinations.codegen.model.Importable

class DestinationMappingUtils (
    private val resolver: Resolver
) {

    fun getDestinationWrappers(annotation: KSAnnotation): List<Importable>? {
        val ksTypes = annotation.findArgumentValue<ArrayList<KSType>>(DESTINATION_ANNOTATION_WRAPPERS_ARGUMENT)
            ?: return null

        return ksTypes.map {
            if ((it.declaration as? KSClassDeclaration)?.classKind != ClassKind.OBJECT) {
                throw IllegalDestinationsSetup("DestinationWrappers need to be objects! (check ${it.declaration.simpleName.asString()})")
            }

            Importable(
                it.declaration.simpleName.asString(),
                it.declaration.qualifiedName!!.asString()
            )
        }
    }

    fun getDestinationStyleType(
        annotation: KSAnnotation,
        locationError: String,
        allowNothing: Boolean = false // if true, will consider nothing as null, else it will throw exception
    ): DestinationStyleType? {
        val ksStyleType = annotation.findArgumentValue<KSType>(DESTINATION_ANNOTATION_STYLE_ARGUMENT)
            ?: return null

        if (allowNothing) {
            if ((ksStyleType.declaration as? KSClassDeclaration)?.isNothing == true) {
                return null
            }
        }

        if (defaultStyle.isAssignableFrom(ksStyleType)) {
            return DestinationStyleType.Default
        }

        if (bottomSheetStyle != null && bottomSheetStyle!!.isAssignableFrom(ksStyleType)) {
            return DestinationStyleType.BottomSheet
        }

        val importable = ksStyleType.findActualClassDeclaration()?.toImportable()
            ?: throw IllegalDestinationsSetup("Parameter $DESTINATION_ANNOTATION_STYLE_ARGUMENT of Destination annotation in $locationError was not resolvable: please review it.")

        if (dialogStyle.isAssignableFrom(ksStyleType)) {
            return DestinationStyleType.Dialog(importable)
        }

        if (animatedStyle != null && animatedStyle!!.isAssignableFrom(ksStyleType)) {
            return DestinationStyleType.Animated(importable, ksStyleType.declaration.findAllRequireOptInAnnotations())
        }

        throw IllegalDestinationsSetup("Unknown style used on $locationError. Please recheck it.")
    }

    private val defaultStyle by lazy {
        resolver.getClassDeclarationByName("$CORE_PACKAGE_NAME.spec.DestinationStyle.Default")!!
            .asType(emptyList())
    }

    private val bottomSheetStyle by lazy {
        resolver.getClassDeclarationByName("$CORE_PACKAGE_NAME.bottomsheet.spec.DestinationStyleBottomSheet")?.asType(emptyList())
    }

    private val animatedStyle by lazy {
        resolver.getClassDeclarationByName("$CORE_PACKAGE_NAME.spec.DestinationStyle.Animated")?.asType(emptyList())
    }

    private val dialogStyle by lazy {
        resolver.getClassDeclarationByName("$CORE_PACKAGE_NAME.spec.DestinationStyle.Dialog")!!.asType(emptyList())
    }
}

