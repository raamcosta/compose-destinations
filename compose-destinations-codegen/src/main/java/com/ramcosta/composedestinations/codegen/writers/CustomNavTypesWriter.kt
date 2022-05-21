package com.ramcosta.composedestinations.codegen.writers

import com.ramcosta.composedestinations.codegen.codeGenBasePackageName
import com.ramcosta.composedestinations.codegen.commons.*
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
    private val typesForNavTypeName: MutableMap<Type, CustomNavType> = mutableMapOf()

    fun write(
        destinations: List<DestinationGeneratingParamsWithNavArgs>,
        navTypeSerializers: List<NavTypeSerializer>
    ): Map<Type, CustomNavType> {
        val serializersByType: Map<Importable, NavTypeSerializer> =
            navTypeSerializers.associateBy { it.genericType }

        val allNavTypeParams: Set<Type> = destinations
            .map {
                it.navArgs
                    .filter { param ->
                        param.isCustomTypeNavArg()
                    }
                    .map { param ->
                        param.type.value
                    }
            }
            .flatten()
            .toSet()

        val enumTypesToGenerate = mutableSetOf<Type>()

        allNavTypeParams
            .toMutableList()
            // if we have multiple classes with equal simple name, ordering by the package length,
            // will make it so we first create the one with the simplest package name, then
            // the other one will have a more meaningful name deduced from the package (since it is longer)
            .apply {
                sortBy {
                    val typeArgLength = (it.typeArguments.firstOrNull() as? TypeArgument.Typed)
                        ?.type?.value?.importable?.qualifiedName?.length

                    it.importable.qualifiedName.length + (typeArgLength ?: 0)
                }
            }
            .forEach { type ->
                when {
                    type.isEnum || (type.isCustomArrayOrArrayListTypeNavArg() && type.firstTypeArg.isEnum) -> {
                        // For enums, we add them all in same file after the non enums process
                        enumTypesToGenerate.add(type)
                    }
                    type.isCustomArrayOrArrayListTypeNavArg() -> {
                        type.generateCustomNavType(serializersByType[type.firstTypeArg.importable])
                    }
                    else -> {
                        type.generateCustomNavType(serializersByType[type.importable])
                    }
                }
            }

        generateEnumCustomTypesFile(enumTypesToGenerate)

        return typesForNavTypeName
    }

    private fun generateEnumCustomTypesFile(typesToGenerate: Set<Type>) {
        if (typesToGenerate.isEmpty()) {
            return
        }

        codeGenerator.makeFile(
            "EnumCustomNavTypes",
            "$codeGenBasePackageName.navtype",
        ).use { out ->
            val allEnums = StringBuilder()
            val typeImports = StringBuilder()
            allEnums += """
                package $codeGenBasePackageName.navtype
                
                import $CORE_PACKAGE_NAME.navargs.primitives.DestinationsEnumNavType
                import $CORE_PACKAGE_NAME.navargs.primitives.array.DestinationsEnumArrayNavType
                import $CORE_PACKAGE_NAME.navargs.primitives.arraylist.DestinationsEnumArrayListNavType
                import $CORE_PACKAGE_NAME.navargs.primitives.valueOfIgnoreCase%s1
            """.trimIndent()

            typesToGenerate.forEach {
                val navTypeName = it.getNavTypeName()

                val importable = if (it.isArrayOrArrayList()) {
                    it.firstTypeArg.importable
                } else it.importable

                if (!typeImports.contains(importable.qualifiedName)) {
                    typeImports += "\nimport ${importable.qualifiedName.sanitizePackageName()}"
                }
                val instantiateNavType = when {
                    it.isArrayList() -> "DestinationsEnumArrayListNavType(${importable.simpleName}::class.java)"
                    it.isArray() -> "DestinationsEnumArrayNavType { Array<${importable.simpleName}>(it.size) { idx -> ${importable.simpleName}::class.java.valueOfIgnoreCase(it[idx]) } }"
                    else -> "DestinationsEnumNavType(${importable.simpleName}::class.java)"
                }

                allEnums += "\n\nval $navTypeName = $instantiateNavType"

                typesForNavTypeName[it] = CustomNavType(navTypeName, null)
            }

            out += allEnums.toString()
                .replace("%s1", typeImports.toString())
        }
    }

    private fun Type.generateCustomNavType(navTypeSerializer: NavTypeSerializer?) {
        val navTypeName = getNavTypeName()

        val className = navTypeName.replaceFirstChar { it.uppercase(Locale.US) }
        val out: OutputStream = codeGenerator.makeFile(
            className,
            "$codeGenBasePackageName.navtype",
        )

        typesForNavTypeName[this] = CustomNavType(navTypeName, navTypeSerializer)

        when {
            isArray() -> generateArrayCustomNavType(
                out,
                className,
                navTypeName,
                navTypeSerializer
            )

            isArrayList() -> generateArrayListCustomNavType(
                out,
                className,
                navTypeName,
                navTypeSerializer
            )

            isSerializable -> generateSerializableCustomNavType(
                className,
                navTypeSerializer,
                out,
                navTypeName,
            )

            isParcelable -> generateParcelableCustomNavType(
                className,
                navTypeSerializer,
                out,
                navTypeName,
            )

            navTypeSerializer != null -> generateCustomTypeSerializerNavType(
                className,
                navTypeSerializer,
                out,
                navTypeName,
            )

            isKtxSerializable -> generateKtxSerializableCustomNavType(
                className,
                out,
                navTypeName,
            )
        }
    }

    private fun Type.generateArrayCustomNavType(
        out: OutputStream,
        className: String,
        navTypeName: String,
        navTypeSerializer: NavTypeSerializer?
    ) {
        val typeArg = firstTypeArg

        val templateWithReplacements = when {
            typeArg.isParcelable -> TemplateWithReplacements(
                template = parcelableArrayNavTypeTemplate,
                additionalImports = parcelableAdditionalImports(typeArg, navTypeSerializer),
                navTypeSerializerInit = typeArg.parcelableNavTypeSerializerCode(navTypeSerializer),
                serializerTypeArg = if (navTypeSerializer == null) "Parcelable" else typeArg.importable.simpleName
            )

            typeArg.isSerializable -> TemplateWithReplacements(
                template = serializableArrayNavTypeTemplate,
                additionalImports = serializableAdditionalImports(typeArg, navTypeSerializer),
                navTypeSerializerInit = serializableNavTypeSerializerCode(navTypeSerializer),
                serializerTypeArg = if (navTypeSerializer == null) "Serializable" else typeArg.importable.simpleName
            )

            navTypeSerializer != null -> TemplateWithReplacements(
                template = customTypeArrayNavTypeTemplate,
                additionalImports = customTypeSerializerAdditionalImports(typeArg, navTypeSerializer),
                navTypeSerializerInit = navTypeSerializerCode(navTypeSerializer),
                serializerTypeArg = typeArg.importable.simpleName,
            )

            typeArg.isKtxSerializable -> TemplateWithReplacements(
                template = ktxSerializableArrayNavTypeTemplate,
                additionalImports = ktxSerializableAdditionalImports(typeArg),
                navTypeSerializerInit = "DefaultKtxSerializableNavTypeSerializer(${typeArg.importable.simpleName}.serializer())",
                serializerTypeArg = typeArg.importable.simpleName,
            )

            else -> error("Unexpected typeArg $typeArg")
        }

        with(templateWithReplacements) {
            out += template
                .replace(TYPE_ARG_CLASS_SIMPLE_NAME, typeArg.importable.simpleName)
                .replace(ARRAY_CUSTOM_NAV_TYPE_NAME, className)
                .replace(SERIALIZER_TYPE_ARG_CLASS_SIMPLE_NAME, serializerTypeArg)
                .replace(
                    NAV_TYPE_INITIALIZATION_CODE,
                    "val $navTypeName = $className($navTypeSerializerInit)\n"
                )
                .replace(ADDITIONAL_IMPORTS, additionalImports)

            out.close()
        }
    }

    private fun Type.generateArrayListCustomNavType(
        out: OutputStream,
        className: String,
        navTypeName: String,
        navTypeSerializer: NavTypeSerializer?
    ) {
        val typeArg = firstTypeArg

        val templateWithReplacements = when {
            typeArg.isParcelable -> TemplateWithReplacements(
                template = parcelableArrayListNavTypeTemplate,
                additionalImports = parcelableAdditionalImports(typeArg, navTypeSerializer),
                navTypeSerializerInit = typeArg.parcelableNavTypeSerializerCode(navTypeSerializer),
                serializerTypeArg = if (navTypeSerializer == null) "Parcelable" else typeArg.importable.simpleName
            )

            typeArg.isSerializable -> TemplateWithReplacements(
                template = serializableArrayListNavTypeTemplate,
                additionalImports = serializableAdditionalImports(typeArg, navTypeSerializer),
                navTypeSerializerInit = serializableNavTypeSerializerCode(navTypeSerializer),
                serializerTypeArg = if (navTypeSerializer == null) "Serializable" else typeArg.importable.simpleName
            )

            navTypeSerializer != null -> TemplateWithReplacements(
                template = customTypeArrayListNavTypeTemplate,
                additionalImports = customTypeSerializerAdditionalImports(typeArg, navTypeSerializer),
                navTypeSerializerInit = navTypeSerializerCode(navTypeSerializer),
                serializerTypeArg = typeArg.importable.simpleName,
            )

            typeArg.isKtxSerializable -> TemplateWithReplacements(
                template = ktxSerializableArrayListNavTypeTemplate,
                additionalImports = ktxSerializableAdditionalImports(typeArg),
                navTypeSerializerInit = "DefaultKtxSerializableNavTypeSerializer(${typeArg.importable.simpleName}.serializer())",
                serializerTypeArg = typeArg.importable.simpleName,
            )

            else -> error("Unexpected typeArg $typeArg")
        }

        with(templateWithReplacements) {
            out += template
                .replace(TYPE_ARG_CLASS_SIMPLE_NAME, typeArg.importable.simpleName)
                .replace(ARRAY_CUSTOM_NAV_TYPE_NAME, className)
                .replace(SERIALIZER_TYPE_ARG_CLASS_SIMPLE_NAME, serializerTypeArg)
                .replace(
                    NAV_TYPE_INITIALIZATION_CODE,
                    "val $navTypeName = $className($navTypeSerializerInit)\n"
                )
                .replace(ADDITIONAL_IMPORTS, additionalImports)

            out.close()
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
            .replace(CLASS_SIMPLE_NAME_CAMEL_CASE, importable.simpleName)
            .replace(
                PARSE_VALUE_CAST_TO_CLASS,
                if (navTypeSerializer == null) " as ${importable.simpleName}" else ""
            )
            .replace(
                DESTINATIONS_NAV_TYPE_SERIALIZER_TYPE,
                if (navTypeSerializer == null) "Serializable" else importable.simpleName
            )
            .replace(ADDITIONAL_IMPORTS, serializableAdditionalImports(this, navTypeSerializer))

        out.close()
    }

    private fun Type.generateKtxSerializableCustomNavType(
        navTypeClassName: String,
        out: OutputStream,
        navTypeName: String
    ) {
        out += ktxSerializableNavTypeTemplate
            .replace(NAV_TYPE_NAME, navTypeName)
            .replace(NAV_TYPE_CLASS_SIMPLE_NAME, navTypeClassName)
            .replace(
                SERIALIZER_SIMPLE_CLASS_NAME,
                "DefaultKtxSerializableNavTypeSerializer(${importable.simpleName}.serializer())"
            )
            .replace(CLASS_SIMPLE_NAME_CAMEL_CASE, importable.simpleName)
            .replace(
                DESTINATIONS_NAV_TYPE_SERIALIZER_TYPE,
                importable.simpleName,
            )
            .replace(ADDITIONAL_IMPORTS, ktxSerializableAdditionalImports(this))

        out.close()
    }

    private fun Type.generateCustomTypeSerializerNavType(
        navTypeClassName: String,
        navTypeSerializer: NavTypeSerializer,
        out: OutputStream,
        navTypeName: String
    ) {
        out += customTypeSerializerNavTypeTemplate
            .replace(NAV_TYPE_NAME, navTypeName)
            .replace(NAV_TYPE_CLASS_SIMPLE_NAME, navTypeClassName)
            .replace(
                SERIALIZER_SIMPLE_CLASS_NAME,
                navTypeSerializerCode(navTypeSerializer)
            )
            .replace(CLASS_SIMPLE_NAME_CAMEL_CASE, importable.simpleName)
            .replace(DESTINATIONS_NAV_TYPE_SERIALIZER_TYPE, importable.simpleName)
            .replace(
                ADDITIONAL_IMPORTS,
                customTypeSerializerAdditionalImports(this, navTypeSerializer),
            )

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
            .replace(CLASS_SIMPLE_NAME_CAMEL_CASE, importable.simpleName)
            .replace(
                PARSE_VALUE_CAST_TO_CLASS,
                if (navTypeSerializer == null) " as ${importable.simpleName}" else ""
            )
            .replace(
                DESTINATIONS_NAV_TYPE_SERIALIZER_TYPE,
                if (navTypeSerializer == null) "Parcelable" else importable.simpleName
            )
            .replace(ADDITIONAL_IMPORTS, parcelableAdditionalImports(this, navTypeSerializer))

        out.close()
    }

    private fun Type.parcelableNavTypeSerializerCode(navTypeSerializer: NavTypeSerializer?): String {
        if (navTypeSerializer == null) {
            return "DefaultParcelableNavTypeSerializer(${this.importable.simpleName}::class.java)"
        }

        return navTypeSerializerCode(navTypeSerializer)
    }

    private fun serializableNavTypeSerializerCode(navTypeSerializer: NavTypeSerializer?): String {
        if (navTypeSerializer == null) {
            return "DefaultSerializableNavTypeSerializer()"
        }

        return navTypeSerializerCode(navTypeSerializer)
    }

    private fun navTypeSerializerCode(navTypeSerializer: NavTypeSerializer): String {
        val simpleName = navTypeSerializer.serializerType.simpleName

        return if (navTypeSerializer.classKind == ClassKind.CLASS) "$simpleName()" else simpleName
    }


    private fun parcelableAdditionalImports(
        type: Type,
        customSerializer: NavTypeSerializer?
    ): String {
        var imports = "\nimport ${type.importable.qualifiedName.sanitizePackageName()}"
        imports += if (customSerializer != null) {
            "\nimport ${customSerializer.serializerType.qualifiedName.sanitizePackageName()}"
        } else {
            "\nimport $CORE_PACKAGE_NAME.navargs.parcelable.DefaultParcelableNavTypeSerializer"
        }

        return imports
    }

    private fun serializableAdditionalImports(
        type: Type,
        customSerializer: NavTypeSerializer?
    ): String {
        var imports = "\nimport ${type.importable.qualifiedName.sanitizePackageName()}"
        imports += if (customSerializer != null) {
            "\nimport ${customSerializer.serializerType.qualifiedName.sanitizePackageName()}"
        } else {
            "\nimport $CORE_PACKAGE_NAME.navargs.serializable.DefaultSerializableNavTypeSerializer"
        }

        return imports
    }

    private fun ktxSerializableAdditionalImports(
        type: Type
    ): String = """
        import ${type.importable.qualifiedName.sanitizePackageName()}
        import ${codeGenBasePackageName}.navargs.ktxserializable.DefaultKtxSerializableNavTypeSerializer
    """.trimIndent()

    private fun customTypeSerializerAdditionalImports(
        type: Type,
        customSerializer: NavTypeSerializer,
    ): String = """
        import ${type.importable.qualifiedName.sanitizePackageName()}
        import ${customSerializer.serializerType.qualifiedName.sanitizePackageName()}
    """.trimIndent()

    private fun Type.getNavTypeName(): String {
        val importableToUse = if (isCustomArrayOrArrayListTypeNavArg()) {
            firstTypeArg.importable
        } else {
            importable
        }

        val navTypeName =
            "${importableToUse.simpleName.replaceFirstChar { it.lowercase(Locale.US) }}${
                if (isEnum || (isCustomArrayOrArrayListTypeNavArg() && firstTypeArg.isEnum)) "Enum" 
                else ""
            }${
                when {
                    isArray() -> "Array"
                    isArrayList() -> "ArrayList"
                    else -> ""
                }
            }NavType"

        val duplicateType = typesForNavTypeName.entries.find { it.value.name == navTypeName }?.key

        val prefix = if (duplicateType != null) {
            val qualifiedNameParts = importableToUse.qualifiedName.split(".").reversed()
            val dupImportableToUse = if (duplicateType.isCustomArrayOrArrayListTypeNavArg()) {
                duplicateType.firstTypeArg.importable
            } else {
                duplicateType.importable
            }
            val duplicateQualifiedNameParts = dupImportableToUse.qualifiedName.split(".").reversed()

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

        return prefix.replaceFirstChar { it.lowercase(Locale.US) } + if (prefix.isNotEmpty()) {
            navTypeName.replaceFirstChar { it.uppercase(Locale.US) }
        } else {
            navTypeName
        }
    }

    class TemplateWithReplacements(
        val template: String,
        val additionalImports: String,
        val navTypeSerializerInit: String,
        val serializerTypeArg: String
    )

}
