package com.ramcosta.composedestinations.codegen.writers

import com.ramcosta.composedestinations.codegen.codeGenBasePackageName
import com.ramcosta.composedestinations.codegen.commons.CORE_PACKAGE_NAME
import com.ramcosta.composedestinations.codegen.commons.isComplexTypeNavArg
import com.ramcosta.composedestinations.codegen.commons.plusAssign
import com.ramcosta.composedestinations.codegen.facades.CodeOutputStreamMaker
import com.ramcosta.composedestinations.codegen.facades.Logger
import com.ramcosta.composedestinations.codegen.model.*
import com.ramcosta.composedestinations.codegen.templates.*
import java.io.OutputStream
import java.util.*

class CustomNavTypesWriter(
    private val codeGenerator: CodeOutputStreamMaker,
    private val logger: Logger
) {
    private val typesForNavTypeName: MutableMap<CustomNavType, ClassType> = mutableMapOf()

    fun write(
        destinations: List<DestinationGeneratingParamsWithNavArgs>,
        navTypeSerializers: List<NavTypeSerializer>
    ): Map<ClassType, CustomNavType> {
        val serializersByType: Map<ClassType, NavTypeSerializer> =
            navTypeSerializers.associateBy { it.genericType }

        val allNavTypeParams: Set<Type> = destinations
            .map {
                it.navArgs
                    .filter { it.isComplexTypeNavArg() }
                    .map { it.type }
            }
            .flatten()
            .toSet()

        allNavTypeParams
            .toMutableList()
            // if we have multiple classes with equal simple name, ordering by the package length,
            // will make it so we first create the one with the simplest package name, then
            // the other one will have a more meaningful name deduced from the package (since it is longer)
            .apply { sortBy { it.classType.qualifiedName.length } }
            .forEach { type ->
                type.generateCustomNavType(serializersByType[type.classType])
            }

        return typesForNavTypeName.entries.associate { it.value to it.key }
    }

    private fun Type.generateCustomNavType(navTypeSerializer: NavTypeSerializer?) {
        val navTypeName = getNavTypeName()

        val className = navTypeName.replaceFirstChar { it.uppercase(Locale.getDefault()) }
        val out: OutputStream = codeGenerator.makeFile(
            className,
            "$codeGenBasePackageName.navtype",
        )

        typesForNavTypeName[CustomNavType(navTypeName, navTypeSerializer)] = classType

        if (isSerializable) {
            generateSerializableCustomNavType(className, navTypeSerializer, out, navTypeName)
        } else if (isParcelable) {
            generateParcelableCustomNavType(className, navTypeSerializer, out, navTypeName)
        }
    }

    private fun Type.generateSerializableCustomNavType(
        navTypeClassName: String,
        navTypeSerializer: NavTypeSerializer?,
        out: OutputStream,
        navTypeName: String
    ) {
        out += serializableNavTypeTemplate
            .replace(NAV_TYPE_NAME, navTypeName)
            .replace(NAV_TYPE_CLASS_SIMPLE_NAME, navTypeClassName)
            .replace(
                SERIALIZER_SIMPLE_CLASS_NAME,
                serializableNavTypeSerializerCode(navTypeSerializer)
            )
            .replace(CLASS_SIMPLE_NAME_CAMEL_CASE, classType.simpleName)
            .replace(
                PARSE_VALUE_CAST_TO_CLASS,
                if (navTypeSerializer == null) " as ${classType.simpleName}" else ""
            )
            .replace(
                SERIALIZE_VALUE_CAST_TO_CLASS,
                if (navTypeSerializer == null) "" else " as ${classType.simpleName}"
            )
            .replace(
                DESTINATIONS_NAV_TYPE_SERIALIZER_TYPE,
                if (navTypeSerializer == null) "Serializable" else classType.simpleName
            )
            .replace(ADDITIONAL_IMPORTS, serializableAdditionalImports(this, navTypeSerializer))

        out.close()
    }

    private fun Type.generateParcelableCustomNavType(
        navTypeClassName: String,
        navTypeSerializer: NavTypeSerializer?,
        out: OutputStream,
        navTypeName: String
    ) {
        out += parcelableNavTypeTemplate
            .replace(NAV_TYPE_NAME, navTypeName)
            .replace(NAV_TYPE_CLASS_SIMPLE_NAME, navTypeClassName)
            .replace(
                SERIALIZER_SIMPLE_CLASS_NAME,
                parcelableNavTypeSerializerCode(navTypeSerializer)
            )
            .replace(CLASS_SIMPLE_NAME_CAMEL_CASE, classType.simpleName)
            .replace(
                PARSE_VALUE_CAST_TO_CLASS,
                if (navTypeSerializer == null) " as ${classType.simpleName}" else ""
            )
            .replace(
                SERIALIZE_VALUE_CAST_TO_CLASS,
                if (navTypeSerializer == null) "" else " as ${classType.simpleName}"
            )
            .replace(
                DESTINATIONS_NAV_TYPE_SERIALIZER_TYPE,
                if (navTypeSerializer == null) "Parcelable" else classType.simpleName
            )
            .replace(ADDITIONAL_IMPORTS, parcelableAdditionalImports(this, navTypeSerializer))

        out.close()
    }

    private fun parcelableNavTypeSerializerCode(navTypeSerializer: NavTypeSerializer?): String {
        if (navTypeSerializer == null) {
            return "DefaultParcelableNavTypeSerializer()"
        }
        val simpleName = navTypeSerializer.serializerType.simpleName

        return if (navTypeSerializer.classKind == ClassKind.CLASS) "$simpleName()" else simpleName
    }

    private fun serializableNavTypeSerializerCode(navTypeSerializer: NavTypeSerializer?): String {
        if (navTypeSerializer == null) {
            return "DefaultSerializableNavTypeSerializer()"
        }
        val simpleName = navTypeSerializer.serializerType.simpleName

        return if (navTypeSerializer.classKind == ClassKind.CLASS) "$simpleName()" else simpleName
    }

    private fun parcelableAdditionalImports(
        type: Type,
        customSerializer: NavTypeSerializer?
    ): String {
        var imports = "\nimport ${type.classType.qualifiedName}"
        imports += if (customSerializer != null) {
            "\nimport ${customSerializer.serializerType.qualifiedName}"
        } else {
            "\nimport $CORE_PACKAGE_NAME.navargs.parcelable.DefaultParcelableNavTypeSerializer"
        }

        return imports
    }

    private fun serializableAdditionalImports(
        type: Type,
        customSerializer: NavTypeSerializer?
    ): String {
        var imports = "\nimport ${type.classType.qualifiedName}"
        imports += if (customSerializer != null) {
            "\nimport ${customSerializer.serializerType.qualifiedName}"
        } else {
            "\nimport $CORE_PACKAGE_NAME.navargs.serializable.DefaultSerializableNavTypeSerializer"
        }

        return imports
    }

    private fun Type.getNavTypeName(): String {
        val navTypeName =
            "${classType.simpleName.replaceFirstChar { it.lowercase(Locale.getDefault()) }}NavType"

        val duplicateType = typesForNavTypeName.entries.find { it.key.name == navTypeName }?.value

        val prefix = if (duplicateType != null) {
            val qualifiedNameParts = classType.qualifiedName.split(".").reversed()
            val duplicateQualifiedNameParts = duplicateType.qualifiedName.split(".").reversed()

            var found: String? = null
            for (qualifiedNamePart in qualifiedNameParts.withIndex()) {
                if (qualifiedNamePart.value != duplicateQualifiedNameParts[qualifiedNamePart.index]) {
                    found = qualifiedNamePart.value
                    break
                }
            }
            found!!
        } else {
            ""
        }

        return prefix + if (prefix.isNotEmpty()) {
            navTypeName.replaceFirstChar { it.uppercase(Locale.getDefault()) }
        } else {
            navTypeName
        }
    }
}