package com.ramcosta.composedestinations.codegen.writers

import com.ramcosta.composedestinations.codegen.codeGenBasePackageName
import com.ramcosta.composedestinations.codegen.commons.*
import com.ramcosta.composedestinations.codegen.facades.CodeOutputStreamMaker
import com.ramcosta.composedestinations.codegen.facades.Logger
import com.ramcosta.composedestinations.codegen.model.*
import com.ramcosta.composedestinations.codegen.templates.*
import com.ramcosta.composedestinations.codegen.templates.core.FileTemplate
import com.ramcosta.composedestinations.codegen.templates.navtype.*
import com.ramcosta.composedestinations.codegen.templates.navtype.arrays.*
import com.ramcosta.composedestinations.codegen.writers.helpers.ImportableHelper
import com.ramcosta.composedestinations.codegen.writers.helpers.writeSourceFile
import java.io.OutputStream
import java.util.*

class CustomNavTypesWriter(
    private val codeGenerator: CodeOutputStreamMaker,
) {
    private val typesForNavTypeName: MutableMap<Type, CustomNavType> = mutableMapOf()
    private val parcelableImportable = Importable(
        "Parcelable",
        "android.os.Parcelable"
    )
    private val serializableImportable = Importable(
        "Serializable",
        "java.io.Serializable"
    )

    fun write(
        destinations: List<DestinationGeneratingParamsWithNavArgs>,
        navTypeSerializers: List<NavTypeSerializer>,
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
        navTypeSerializer: NavTypeSerializer?,
    ) {
        val typeArg = firstTypeArg

        val templateWithReplacements = when {
            typeArg.isParcelable -> {
                val importableHelper = ImportableHelper(parcelableArrayNavTypeTemplate.imports)
                TemplateWithReplacements(
                    template = parcelableArrayNavTypeTemplate,
                    importableHelper = importableHelper.parcelableAdditionalImports(typeArg, navTypeSerializer),
                    navTypeSerializerInit = typeArg.parcelableNavTypeSerializerCode(navTypeSerializer),
                    serializerTypeArg = if (navTypeSerializer == null) importableHelper.addAndGetPlaceholder(parcelableImportable)
                    else importableHelper.addAndGetPlaceholder(typeArg.importable)
                )
            }

            typeArg.isSerializable -> {
                val importableHelper = ImportableHelper(serializableArrayNavTypeTemplate.imports)
                TemplateWithReplacements(
                    template = serializableArrayNavTypeTemplate,
                    importableHelper = importableHelper.serializableAdditionalImports(typeArg, navTypeSerializer),
                    navTypeSerializerInit = serializableNavTypeSerializerCode(navTypeSerializer),
                    serializerTypeArg = if (navTypeSerializer == null) importableHelper.addAndGetPlaceholder(serializableImportable)
                    else importableHelper.addAndGetPlaceholder(typeArg.importable)
                )
            }

            navTypeSerializer != null -> {
                val importableHelper = ImportableHelper(customTypeArrayNavTypeTemplate.imports)
                TemplateWithReplacements(
                    template = customTypeArrayNavTypeTemplate,
                    importableHelper = importableHelper.customTypeSerializerAdditionalImports(typeArg, navTypeSerializer),
                    navTypeSerializerInit = navTypeSerializerCode(navTypeSerializer),
                    serializerTypeArg = importableHelper.addAndGetPlaceholder(typeArg.importable),
                )
            }

            typeArg.isKtxSerializable -> {
                val importableHelper = ImportableHelper(ktxSerializableArrayNavTypeTemplate.imports)
                TemplateWithReplacements(
                    template = ktxSerializableArrayNavTypeTemplate,
                    importableHelper = importableHelper.ktxSerializableAdditionalImports(typeArg),
                    navTypeSerializerInit = "DefaultKtxSerializableNavTypeSerializer(${importableHelper.addAndGetPlaceholder(typeArg.importable)}.serializer())",
                    serializerTypeArg = importableHelper.addAndGetPlaceholder(typeArg.importable),
                )
            }

            else -> {
                error("Unexpected typeArg $typeArg")
            }
        }

        with(templateWithReplacements) {
            out.writeSourceFile(
                packageStatement = template.packageStatement,
                importableHelper = importableHelper,
                sourceCode = template.sourceCode
                    .replace(TYPE_ARG_CLASS_SIMPLE_NAME, typeArg.importable.simpleName)
                    .replace(ARRAY_CUSTOM_NAV_TYPE_NAME, className)
                    .replace(SERIALIZER_TYPE_ARG_CLASS_SIMPLE_NAME, serializerTypeArg)
                    .replace(
                        NAV_TYPE_INITIALIZATION_CODE,
                        "val $navTypeName = $className($navTypeSerializerInit)\n"
                    )
            )
        }
    }

    private fun Type.generateArrayListCustomNavType(
        out: OutputStream,
        className: String,
        navTypeName: String,
        navTypeSerializer: NavTypeSerializer?,
    ) {
        val typeArg = firstTypeArg

        val templateWithReplacements = when {
            typeArg.isParcelable -> {
                val importableHelper = ImportableHelper(parcelableArrayListNavTypeTemplate.imports)
                TemplateWithReplacements(
                    template = parcelableArrayListNavTypeTemplate,
                    importableHelper = importableHelper.parcelableAdditionalImports(typeArg,
                        navTypeSerializer),
                    navTypeSerializerInit = typeArg.parcelableNavTypeSerializerCode(
                        navTypeSerializer),
                    serializerTypeArg = if (navTypeSerializer == null) importableHelper.addAndGetPlaceholder(
                        parcelableImportable)
                    else importableHelper.addAndGetPlaceholder(typeArg.importable)
                )
            }

            typeArg.isSerializable -> {
                val importableHelper =
                    ImportableHelper(serializableArrayListNavTypeTemplate.imports)
                TemplateWithReplacements(
                    template = serializableArrayListNavTypeTemplate,
                    importableHelper = importableHelper.serializableAdditionalImports(typeArg,
                        navTypeSerializer),
                    navTypeSerializerInit = serializableNavTypeSerializerCode(navTypeSerializer),
                    serializerTypeArg = if (navTypeSerializer == null) importableHelper.addAndGetPlaceholder(
                        serializableImportable)
                    else importableHelper.addAndGetPlaceholder(typeArg.importable)
                )
            }

            navTypeSerializer != null -> {
                val importableHelper = ImportableHelper(customTypeArrayListNavTypeTemplate.imports)
                TemplateWithReplacements(
                    template = customTypeArrayListNavTypeTemplate,
                    importableHelper = importableHelper.customTypeSerializerAdditionalImports(
                        typeArg,
                        navTypeSerializer),
                    navTypeSerializerInit = navTypeSerializerCode(navTypeSerializer),
                    serializerTypeArg = importableHelper.addAndGetPlaceholder(typeArg.importable),
                )
            }

            typeArg.isKtxSerializable -> {
                val importableHelper =
                    ImportableHelper(ktxSerializableArrayListNavTypeTemplate.imports)
                TemplateWithReplacements(
                    template = ktxSerializableArrayListNavTypeTemplate,
                    importableHelper = importableHelper.ktxSerializableAdditionalImports(typeArg),
                    navTypeSerializerInit = "DefaultKtxSerializableNavTypeSerializer(${importableHelper.addAndGetPlaceholder(typeArg.importable)}.serializer())",
                    serializerTypeArg = importableHelper.addAndGetPlaceholder(typeArg.importable),
                )
            }

            else -> {
                error("Unexpected typeArg $typeArg")
            }
        }

        with(templateWithReplacements) {
            out.writeSourceFile(
                packageStatement = template.packageStatement,
                importableHelper = importableHelper,
                sourceCode = template.sourceCode
                    .replace(TYPE_ARG_CLASS_SIMPLE_NAME, typeArg.importable.simpleName)
                    .replace(ARRAY_CUSTOM_NAV_TYPE_NAME, className)
                    .replace(SERIALIZER_TYPE_ARG_CLASS_SIMPLE_NAME, serializerTypeArg)
                    .replace(
                        NAV_TYPE_INITIALIZATION_CODE,
                        "val $navTypeName = $className($navTypeSerializerInit)\n"
                    )
            )
        }
    }

    private fun Type.generateSerializableCustomNavType(
        navTypeClassName: String,
        navTypeSerializer: NavTypeSerializer?,
        out: OutputStream,
        navTypeName: String,
    ) {
        val importableHelper = ImportableHelper(serializableNavTypeTemplate.imports)

        out.writeSourceFile(
            packageStatement = serializableNavTypeTemplate.packageStatement,
            importableHelper = importableHelper.serializableAdditionalImports(
                type = this,
                customSerializer = navTypeSerializer
            ),
            sourceCode = serializableNavTypeTemplate.sourceCode
                .replace(NAV_TYPE_NAME, navTypeName)
                .replace(NAV_TYPE_CLASS_SIMPLE_NAME, navTypeClassName)
                .replace(
                    SERIALIZER_SIMPLE_CLASS_NAME,
                    serializableNavTypeSerializerCode(navTypeSerializer)
                )
                .replace(CLASS_SIMPLE_NAME_CAMEL_CASE, importableHelper.addAndGetPlaceholder(importable))
                .replace(
                    PARSE_VALUE_CAST_TO_CLASS,
                    if (navTypeSerializer == null) " as ${importableHelper.addAndGetPlaceholder(importable)}"
                    else ""
                )
                .replace(
                    DESTINATIONS_NAV_TYPE_SERIALIZER_TYPE,
                    if (navTypeSerializer == null) importableHelper.addAndGetPlaceholder(serializableImportable)
                    else importableHelper.addAndGetPlaceholder(importable)
                )
        )
    }

    private fun Type.generateKtxSerializableCustomNavType(
        navTypeClassName: String,
        out: OutputStream,
        navTypeName: String,
    ) {
        val importableHelper = ImportableHelper(ktxSerializableNavTypeTemplate.imports)

        out.writeSourceFile(
            packageStatement = ktxSerializableNavTypeTemplate.packageStatement,
            importableHelper = importableHelper.ktxSerializableAdditionalImports(this),
            sourceCode = ktxSerializableNavTypeTemplate.sourceCode
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
        )
    }

    private fun Type.generateCustomTypeSerializerNavType(
        navTypeClassName: String,
        navTypeSerializer: NavTypeSerializer,
        out: OutputStream,
        navTypeName: String,
    ) {
        val importableHelper = ImportableHelper(customTypeSerializerNavTypeTemplate.imports)
        out.writeSourceFile(
            packageStatement = customTypeSerializerNavTypeTemplate.packageStatement,
            importableHelper = importableHelper.customTypeSerializerAdditionalImports(this, navTypeSerializer),
            sourceCode = customTypeSerializerNavTypeTemplate.sourceCode
                .replace(NAV_TYPE_NAME, navTypeName)
                .replace(NAV_TYPE_CLASS_SIMPLE_NAME, navTypeClassName)
                .replace(
                    SERIALIZER_SIMPLE_CLASS_NAME,
                    navTypeSerializerCode(navTypeSerializer)
                )
                .replace(CLASS_SIMPLE_NAME_CAMEL_CASE, importableHelper.addAndGetPlaceholder(importable))
                .replace(DESTINATIONS_NAV_TYPE_SERIALIZER_TYPE, importableHelper.addAndGetPlaceholder(importable))
        )
    }

    private fun Type.generateParcelableCustomNavType(
        navTypeClassName: String,
        navTypeSerializer: NavTypeSerializer?,
        out: OutputStream,
        navTypeName: String,
    ) {
        val importableHelper = ImportableHelper(parcelableNavTypeTemplate.imports)

        out.writeSourceFile(
            packageStatement = parcelableNavTypeTemplate.packageStatement,
            importableHelper = importableHelper.parcelableAdditionalImports(this, navTypeSerializer),
            sourceCode = parcelableNavTypeTemplate.sourceCode
                .replace(NAV_TYPE_NAME, navTypeName)
                .replace(NAV_TYPE_CLASS_SIMPLE_NAME, navTypeClassName)
                .replace(
                    SERIALIZER_SIMPLE_CLASS_NAME,
                    parcelableNavTypeSerializerCode(navTypeSerializer)
                )
                .replace(CLASS_SIMPLE_NAME_CAMEL_CASE, importableHelper.addAndGetPlaceholder(importable))
                .replace(
                    PARSE_VALUE_CAST_TO_CLASS,
                    if (navTypeSerializer == null) " as ${importableHelper.addAndGetPlaceholder(importable)}"
                    else ""
                )
                .replace(
                    DESTINATIONS_NAV_TYPE_SERIALIZER_TYPE,
                    if (navTypeSerializer == null) importableHelper.addAndGetPlaceholder(parcelableImportable)
                    else importableHelper.addAndGetPlaceholder(importable)
                )
        )
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


    private fun ImportableHelper.parcelableAdditionalImports(
        type: Type,
        customSerializer: NavTypeSerializer?,
    ): ImportableHelper {
        add(parcelableImportable)
        add(type.importable)
        if (customSerializer != null) {
            add(customSerializer.serializerType)
        } else {
            add(Importable(
                "DefaultParcelableNavTypeSerializer",
                "$CORE_PACKAGE_NAME.navargs.parcelable.DefaultParcelableNavTypeSerializer")
            )
        }

        return this
    }

    private fun ImportableHelper.serializableAdditionalImports(
        type: Type,
        customSerializer: NavTypeSerializer?,
    ): ImportableHelper {
        add(type.importable)
        if (customSerializer != null) {
            add(customSerializer.serializerType)
        } else {
            add(Importable(
                "DefaultSerializableNavTypeSerializer",
                "$CORE_PACKAGE_NAME.navargs.serializable.DefaultSerializableNavTypeSerializer")
            )
        }

        return this
    }

    private fun ImportableHelper.ktxSerializableAdditionalImports(
        type: Type,
    ): ImportableHelper {
        add(type.importable)
        add(Importable(
            "DefaultKtxSerializableNavTypeSerializer",
            "${codeGenBasePackageName}.navargs.ktxserializable.DefaultKtxSerializableNavTypeSerializer")
        )

        return this
    }

    private fun ImportableHelper.customTypeSerializerAdditionalImports(
        type: Type,
        customSerializer: NavTypeSerializer,
    ): ImportableHelper {
        add(type.importable)
        add(customSerializer.serializerType)

        return this
    }

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
        val template: FileTemplate,
        val importableHelper: ImportableHelper,
        val navTypeSerializerInit: String,
        val serializerTypeArg: String,
    )

}
