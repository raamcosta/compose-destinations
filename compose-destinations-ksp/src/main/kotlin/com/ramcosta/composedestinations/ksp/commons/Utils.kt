package com.ramcosta.composedestinations.ksp.commons

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.*
import com.ramcosta.composedestinations.codegen.commons.*
import com.ramcosta.composedestinations.codegen.model.*
import com.ramcosta.composedestinations.codegen.model.ClassKind
import com.ramcosta.composedestinations.ksp.processors.KSPClassKind
import java.io.*

fun KSAnnotated.findAnnotation(name: String): KSAnnotation {
    return annotations.find { it.shortName.asString() == name }!!
}

inline fun <reified T> KSAnnotation.findArgumentValue(name: String): T? {
    return arguments.find { it.name?.asString() == name }?.value as T?
}

fun File.readLineAndImports(lineNumber: Int): Pair<String, List<String>> {
    val bufferedReader = BufferedReader(InputStreamReader(FileInputStream(this), Charsets.UTF_8))
    return bufferedReader
        .useLines { lines: Sequence<String> ->
            val firstNLines = lines.take(lineNumber)

            val iterator = firstNLines.iterator()
            var line = iterator.next()
            val importsList = mutableListOf<String>()
            while (iterator.hasNext()) {
                line = iterator.next()
                if (line.startsWith("import")) {
                    importsList.add(line.removePrefix("import "))
                }
            }

            line to importsList
        }
}

fun File.readLine(lineNumber: Int): String {
    val bufferedReader = BufferedReader(InputStreamReader(FileInputStream(this), Charsets.UTF_8))
    return bufferedReader
        .useLines { lines: Sequence<String> ->
            lines
                .take(lineNumber)
                .last()
        }
}

fun KSAnnotated.findAllRequireOptInAnnotations(): List<ClassType> {
    val requireOptInAnnotations = mutableListOf<ClassType>()
    annotations.forEach { annotation ->
        val annotationShortName = annotation.shortName.asString()
        if (annotationShortName == "Composable" || annotationShortName == "Destination") {
            return@forEach
        }

        val ksType = annotation.annotationType.resolve()
        if (ksType.isRequireOptInAnnotation()) {
            requireOptInAnnotations.add(ClassType(annotationShortName, ksType.declaration.qualifiedName!!.asString()))
        }
    }

    return requireOptInAnnotations
}

fun KSType.isRequireOptInAnnotation(): Boolean {
    return declaration.annotations.any { annotation ->
        annotation.shortName.asString() == "RequiresOptIn"
                || annotation.annotationType.annotations.any {
            annotation.annotationType.resolve().isRequireOptInAnnotation()
        }
    }
}

fun Resolver.getNavTypeSerializers(): List<NavTypeSerializer> {
    return getSymbolsWithAnnotation(NAV_TYPE_SERIALIZER_ANNOTATION_QUALIFIED)
        .filterIsInstance<KSClassDeclaration>().map {
            if (it.classKind != KSPClassKind.CLASS && it.classKind != KSPClassKind.OBJECT) {
                throw IllegalDestinationsSetup("${it.simpleName}: Type serializers must be either class or object!")
            }

            var superType: KSType? = null
            var isCustomTypeSerializer = false
            for (type in it.superTypes) {
                val resolvedType = type.resolve()
                val resolvedTypeString = resolvedType.declaration.qualifiedName?.asString()
                if (resolvedTypeString ==
                    "$CORE_PACKAGE_NAME.navargs.parcelable.ParcelableNavTypeSerializer") {
                    superType = resolvedType
                    break
                }

                if (resolvedTypeString ==
                    "$CORE_PACKAGE_NAME.navargs.serializable.SerializableNavTypeSerializer") {
                    superType = resolvedType
                    break
                }

                if (resolvedTypeString == NAV_TYPE_SERIALIZER_CUSTOM_SERIALIZER) {
                    superType = resolvedType
                    isCustomTypeSerializer = true
                    break
                }
            }
            if (superType == null) {
                throw IllegalDestinationsSetup("${it.simpleName}: Type serializers must implement ParcelableNavTypeSerializer (or SerializableNavTypeSerializer for Serializable types)!")
            }
            val genericType = superType.arguments.first().type?.resolve()?.declaration as KSClassDeclaration

            NavTypeSerializer(
                classKind = if (it.classKind == KSPClassKind.CLASS) ClassKind.CLASS else ClassKind.OBJECT,
                serializerType = ClassType(it.simpleName.asString(), it.qualifiedName!!.asString()),
                genericType = ClassType(genericType.simpleName.asString(), genericType.qualifiedName!!.asString()),
                isCustomTypeSerializer = isCustomTypeSerializer,
            )
        }.toList()
}
