package com.ramcosta.composedestinations.ksp.commons

import com.google.devtools.ksp.findActualType
import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.FileLocation
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeAlias
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.Location
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.symbol.Variance
import com.ramcosta.composedestinations.codegen.commons.DESTINATION_ANNOTATION_NAV_ARGS_DELEGATE_ARGUMENT
import com.ramcosta.composedestinations.codegen.commons.IllegalDestinationsSetup
import com.ramcosta.composedestinations.codegen.commons.NAV_HOST_PARAM_ANNOTATION_QUALIFIED
import com.ramcosta.composedestinations.codegen.model.DeepLink
import com.ramcosta.composedestinations.codegen.model.Importable
import com.ramcosta.composedestinations.codegen.model.NavTypeSerializer
import com.ramcosta.composedestinations.codegen.model.Parameter
import com.ramcosta.composedestinations.codegen.model.RawNavArgsClass
import com.ramcosta.composedestinations.codegen.model.Type
import com.ramcosta.composedestinations.codegen.model.TypeArgument
import com.ramcosta.composedestinations.codegen.model.TypeInfo
import com.ramcosta.composedestinations.codegen.model.ValueClassInnerInfo
import com.ramcosta.composedestinations.ksp.processors.KSPClassKind
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader


val ignoreAnnotations = listOf(
    "Composable",
    "Target",
    "Retention",
    "MustBeDocumented",
    "OptIn",
    "RequiresOptIn"
)

fun KSAnnotated.findAnnotation(name: String): KSAnnotation {
    return annotations.find { it.shortName.asString() == name }!!
}

fun KSAnnotated.findAnnotationPathRecursively(name: String, path: List<KSAnnotation> = emptyList()): List<KSAnnotation>? {
    val relevantAnnotations = annotations.filter { it.shortName.asString() !in ignoreAnnotations}
    val foundAnnotation = relevantAnnotations.find { it.shortName.asString() == name }
    if (foundAnnotation != null) {
        return path + foundAnnotation
    }

    relevantAnnotations.forEach { annotation ->
        val found = annotation.annotationType.resolve().declaration.findAnnotationPathRecursively(name, path + annotation)
        if (found != null) {
            return found
        }
    }

    return null
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

fun KSAnnotated.findAllRequireOptInAnnotations(): List<Importable> {
    val requireOptInAnnotations = mutableListOf<Importable>()
    annotations.forEach { annotation ->
        val annotationShortName = annotation.shortName.asString()
        if (annotationShortName == "Composable" || annotationShortName == "Destination") {
            return@forEach
        }

        val ksType = annotation.annotationType.resolve()
        if (ksType.isRequireOptInAnnotation()) {
            requireOptInAnnotations.add(Importable(annotationShortName, ksType.declaration.qualifiedName!!.asString()))
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

fun KSType.findActualClassDeclaration(): KSClassDeclaration? {
    if (this.declaration is KSTypeAlias) {
        return (this.declaration as KSTypeAlias).findActualType()
    }

    return declaration as? KSClassDeclaration?
}

fun KSClassDeclaration.toImportable(): Importable {
    return Importable(
        simpleName.asString(),
        qualifiedName!!.asString()
    )
}

val KSClassDeclaration.isNothing get() =
    qualifiedName?.asString() == "java.lang.Void" || qualifiedName?.asString() == "kotlin.Nothing"

fun KSAnnotation.toDeepLink(): DeepLink {
    return DeepLink(
        findArgumentValue("action")!!,
        findArgumentValue("mimeType")!!,
        findArgumentValue("uriPattern")!!,
    )
}

data class NavArgsTypeWithFile(val type: RawNavArgsClass, val file: KSFile?)

fun KSAnnotation.getNavArgsDelegateType(
    resolver: Resolver,
    navTypeSerializersByType: Map<Importable, NavTypeSerializer>
): NavArgsTypeWithFile? = kotlin.runCatching {
    val ksType = findArgumentValue<KSType>(DESTINATION_ANNOTATION_NAV_ARGS_DELEGATE_ARGUMENT)
        ?: return null

    return ksType.getNavArgsDelegateType(resolver, navTypeSerializersByType)
}.getOrElse {
    throw IllegalDestinationsSetup("There was an issue with '$DESTINATION_ANNOTATION_NAV_ARGS_DELEGATE_ARGUMENT'" +
            " of annotation ${shortName.asString()}: make sure it is a class with a primary constructor.", it)
}

private fun KSType.getNavArgsDelegateType(
    resolver: Resolver,
    navTypeSerializersByType: Map<Importable, NavTypeSerializer>
): NavArgsTypeWithFile? {
    val ksClassDeclaration = declaration as KSClassDeclaration
    if (ksClassDeclaration.isNothing) {
        return null
    }

    val parameters = ksClassDeclaration.primaryConstructor!!
        .parameters
        .map { it.toParameter(resolver, navTypeSerializersByType) }

    return NavArgsTypeWithFile(
        RawNavArgsClass(
            parameters,
            Importable(
                ksClassDeclaration.simpleName.asString(),
                ksClassDeclaration.qualifiedName!!.asString(),
            )
        ),
        ksClassDeclaration.containingFile
    )
}

fun KSValueParameter.toParameter(
    resolver: Resolver,
    navTypeSerializersByType: Map<Importable, NavTypeSerializer>
): Parameter {
    val resolvedType = type.resolve()
    val type = resolvedType.toType(location, resolver, navTypeSerializersByType)
        ?: throw IllegalDestinationsSetup("Parameter \"${name!!.asString()}\" of " +
                "was not resolvable: please review it.")

    return Parameter(
        name = name!!.asString(),
        type = type,
        hasDefault = hasDefault,
        isMarkedNavHostParam = this.annotations.any {
            it.shortName.asString() == "NavHostParam" &&
                    it.annotationType.resolve().declaration.qualifiedName?.asString() == NAV_HOST_PARAM_ANNOTATION_QUALIFIED
        },
        lazyDefaultValue = lazy { getDefaultValue(resolver) }
    )
}

fun KSType.toType(
    location: Location,
    resolver: Resolver,
    navTypeSerializersByType: Map<Importable, NavTypeSerializer>
): TypeInfo? {
    val qualifiedName = declaration.qualifiedName ?: return null

    val ksClassDeclaration = findActualClassDeclaration()
    val classDeclarationType = ksClassDeclaration?.asType(emptyList())

    val importable = Importable(
        ksClassDeclaration?.simpleName?.asString() ?: declaration.simpleName.asString(),
        ksClassDeclaration?.qualifiedName?.asString() ?: qualifiedName.asString()
    )

    return TypeInfo(
        value = Type(
            importable = importable,
            typeArguments = argumentTypes(location, resolver, navTypeSerializersByType),
            requireOptInAnnotations = ksClassDeclaration?.findAllRequireOptInAnnotations() ?: emptyList(),
            isEnum = ksClassDeclaration?.classKind == KSPClassKind.ENUM_CLASS,
            isParcelable = classDeclarationType?.let { resolver.parcelableType().isAssignableFrom(it) } ?: false,
            isSerializable = classDeclarationType?.let { resolver.serializableType().isAssignableFrom(it) } ?: false,
            isKtxSerializable = isKtxSerializable(),
            valueClassInnerInfo = ksClassDeclaration?.valueClassInnerInfo(resolver, navTypeSerializersByType),
        ),
        isNullable = isMarkedNullable,
        hasCustomTypeSerializer = navTypeSerializersByType[importable] != null,
    )
}

private fun KSType.argumentTypes(location: Location, resolver: Resolver, navTypeSerializersByType: Map<Importable, NavTypeSerializer>): List<TypeArgument> {
    return arguments.mapNotNull { typeArg ->
        if (typeArg.variance == Variance.STAR) {
            return@mapNotNull TypeArgument.Star
        }
        val resolvedType = typeArg.type?.resolve()

        if (resolvedType?.isError == true) {
            return@mapNotNull TypeArgument.Error(lazy { getErrorLine(location) })
        }

        resolvedType?.toType(location, resolver, navTypeSerializersByType)?.let { TypeArgument.Typed(it, typeArg.variance.label) }
    }
}

private fun getErrorLine(location: Location): String {
    val fileLocation = location as FileLocation
    return File(fileLocation.filePath).readLine(fileLocation.lineNumber)
}

private var _parcelableType: KSType? = null
private fun Resolver.parcelableType(): KSType {
    return _parcelableType ?: getClassDeclarationByName("android.os.Parcelable")!!.asType(emptyList()).also {
        _parcelableType = it
    }
}

private var _serializableType: KSType? = null
private fun Resolver.serializableType(): KSType {
    return _serializableType ?: getClassDeclarationByName("java.io.Serializable")!!.asType(emptyList()).also {
        _serializableType = it
    }
}


private fun KSType.isKtxSerializable(): Boolean {
    // Check current type annotations
    if (declaration.annotations.isKtxSerializable()) {
        return true
    }

    if (declaration is KSTypeAlias) {
        val typeAlias = declaration as KSTypeAlias
        // For alias check type alias annotations or annotations of reference type
        return typeAlias.type.annotations.isKtxSerializable() || typeAlias.type.resolve().isKtxSerializable()
    }

    return false
}

private fun Sequence<KSAnnotation>.isKtxSerializable(): Boolean =
    any {
        it.annotationType.resolve().declaration.qualifiedName?.asString()
            ?.let { qualifiedName ->
                qualifiedName == "kotlinx.serialization.Serializable"
            } ?: false
    }

private fun KSClassDeclaration.valueClassInnerInfo(resolver: Resolver, navTypeSerializersByType: Map<Importable, NavTypeSerializer>): ValueClassInnerInfo? {
    return if (modifiers.contains(Modifier.VALUE)) {
        // This is a value class, get the inner type's type only once (not recursively)
        val firstArg: KSValueParameter = primaryConstructor!!.parameters.first()
        val valueClassArgType = firstArg.type.resolve()

        val firstArgType = valueClassArgType.toType(valueClassArgType.declaration.location, resolver, navTypeSerializersByType)
        val firstPublicNonNullableArgName = this.getDeclaredProperties().firstOrNull {
            it.isPublic() && it.simpleName.asString() == firstArg.name?.asString() && !it.type.resolve().isMarkedNullable
        }?.simpleName?.asString()

        firstArgType?.let {
            ValueClassInnerInfo(it, primaryConstructor!!.isPublic(), firstPublicNonNullableArgName)
        }
    } else {
        null
    }
}