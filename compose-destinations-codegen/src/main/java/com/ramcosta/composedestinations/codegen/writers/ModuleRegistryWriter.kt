package com.ramcosta.composedestinations.codegen.writers

import com.ramcosta.composedestinations.codegen.codeGenBasePackageName
import com.ramcosta.composedestinations.codegen.commons.CORE_PACKAGE_NAME
import com.ramcosta.composedestinations.codegen.commons.RESULT_BACK_NAVIGATOR_QUALIFIED_NAME
import com.ramcosta.composedestinations.codegen.commons.RawNavGraphTree
import com.ramcosta.composedestinations.codegen.commons.coreTypes
import com.ramcosta.composedestinations.codegen.commons.isCustomTypeNavArg
import com.ramcosta.composedestinations.codegen.commons.toTypeCode
import com.ramcosta.composedestinations.codegen.facades.CodeOutputStreamMaker
import com.ramcosta.composedestinations.codegen.model.CodeGenConfig
import com.ramcosta.composedestinations.codegen.model.CodeGenProcessedDestination
import com.ramcosta.composedestinations.codegen.model.CustomNavType
import com.ramcosta.composedestinations.codegen.model.Importable
import com.ramcosta.composedestinations.codegen.model.SubModuleInfo
import com.ramcosta.composedestinations.codegen.model.Type
import com.ramcosta.composedestinations.codegen.model.TypeArgument
import com.ramcosta.composedestinations.codegen.model.TypeInfo
import com.ramcosta.composedestinations.codegen.model.Visibility
import com.ramcosta.composedestinations.codegen.registryId
import com.ramcosta.composedestinations.codegen.templates.core.setOfImportable
import com.ramcosta.composedestinations.codegen.writers.helpers.ImportableHelper
import com.ramcosta.composedestinations.codegen.writers.helpers.writeSourceFile

internal class ModuleRegistryWriter(
    private val customNavTypeByType: Map<Type, CustomNavType>,
    private val submodules: List<SubModuleInfo>,
    private val codeGenConfig: CodeGenConfig,
    private val codeGenerator: CodeOutputStreamMaker
) {
    companion object {

        private const val packageName = "_generated._ramcosta._composedestinations._moduleregistry"

        internal val navTypeRegistryImportable = Importable(
            "_registerNavTypes_$registryId",
            "$packageName._registerNavTypes_$registryId"
        )
    }

    fun write(
        destinations: List<CodeGenProcessedDestination>,
        graphTrees: List<RawNavGraphTree>
    ) {
        makeModuleRegistryFile(destinations, graphTrees)

        makeNavTypeRegistryFile()
    }

    private fun makeNavTypeRegistryFile() {
        val importableHelper = ImportableHelper(
            setOfImportable(
                "$CORE_PACKAGE_NAME.spec.DestinationSpec",
                "$CORE_PACKAGE_NAME.result.registerComplexNavType",
                "$CORE_PACKAGE_NAME.result.TypeInfo",
                if (customNavTypeByType.isNotEmpty()) "$codeGenBasePackageName.navtype.*"
                else null
            )
        )

        codeGenerator.makeFile(
            "_NavTypeRegister_$registryId",
            packageName
        ).writeSourceFile(
            fileOptIns = setOf(
                Importable(
                    "InternalDestinationsApi",
                    "$CORE_PACKAGE_NAME.annotation.internal.InternalDestinationsApi"
                )
            ),
            packageStatement = "package $packageName",
            importableHelper = importableHelper,
            sourceCode = """
                    public fun _registerNavTypes_$registryId() {
                    %s1
                    }
                """.trimIndent()
                .replace("%s1",
                    customNavTypeByType.entries.joinToString("\n") { (type, navType) ->
                        "\tregisterComplexNavType(${type.toCoreTypeInfoCode(importableHelper)}, ${navType.name})"
                    } + getSubmoduleCallsCode(importableHelper)
                )
        )
    }

    private fun getSubmoduleCallsCode(importableHelper: ImportableHelper): String {
        val subModuleRegistryCalls =
            submodules.map { it.navTypeRegisterFunction }.joinToString("\n") {
                val placeholder =
                    importableHelper.addAndGetPlaceholder(Importable(it.split(".").last(), it))
                "\t$placeholder()"
            }

        return "\n" + subModuleRegistryCalls
    }

    private fun Type.toCoreTypeInfoCode(importableHelper: ImportableHelper): String {
        val typeArgsStr = if (typeArguments.isNotEmpty()) {
            ", arrayOf(${typeArguments.joinToString(", ") { "${it.toTypeCode(importableHelper)}::class" }})"
        } else {
            ""
        }
        return "TypeInfo(${importableHelper.addAndGetPlaceholder(importable)}::class$typeArgsStr)"
    }

    private fun makeModuleRegistryFile(
        destinations: List<CodeGenProcessedDestination>,
        graphTrees: List<RawNavGraphTree>
    ) {
        val resultBackTypesByDestination: List<Pair<CodeGenProcessedDestination, TypeInfo>> =
            destinations.mapNotNull { destination ->
                if (destination.visibility != Visibility.PUBLIC) return@mapNotNull null

                destination.parameters.firstOrNull { it.type.importable.qualifiedName == RESULT_BACK_NAVIGATOR_QUALIFIED_NAME }
                    ?.let {
                        val type =
                            (it.type.typeArguments.firstOrNull() as? TypeArgument.Typed)?.type
                                ?: return@mapNotNull null

                        destination to type
                    }
            }

        val importableHelper = ImportableHelper(
            setOfImportable(
                "$CORE_PACKAGE_NAME.spec.DestinationSpec"
            )
        )

        codeGenerator.makeFile(
            "_ModuleRegistry_$registryId",
            packageName
        ).writeSourceFile(
            packageStatement = "package $packageName",
            importableHelper = importableHelper,
            sourceCode = """
                        public annotation class _Info_$registryId(
                            val moduleName: String,
                            val packageName: String,
                            val hasNavArgsPackage: Boolean,
                            val navTypeRegisterFunction: String,
                            val typeResults: Array<_Destination_Result_Info_$registryId> = emptyArray(),
                            val topLevelGraphs: Array<String> = emptyArray()
                        )
                        
                        public annotation class _Destination_Result_Info_$registryId(
                            val destination: String,
                            val resultType: String,
                            val resultNavType: String,
                            val isResultNullable: Boolean
                        )
                        
                        @_Info_$registryId(
                            moduleName = "${codeGenConfig.moduleName ?: ""}",
                            packageName = "$codeGenBasePackageName",
                            navTypeRegisterFunction = "$packageName._registerNavTypes_$registryId",
                            hasNavArgsPackage = ${"$codeGenBasePackageName.navargs" in codeGenerator.packageNamesWrittenTo},
                            typeResults = [
                        %s1
                            ],
                            topLevelGraphs = [
                        %s2
                            ]
                        )
                        public object _ModuleRegistry_$registryId
                    """.trimIndent()
                .replace(
                    "%s1",
                    resultBackTypesByDestination.joinToString(",\n") { (destination, type) ->
                        """
                            |       _Destination_Result_Info_$registryId(
                            |           destination = "${destination.destinationImportable.qualifiedName}",
                            |           resultType = "${type.importable.qualifiedName}",
                            |           resultNavType = "${if (type.isCustomTypeNavArg()) customNavTypeByType[type.value]!!.importable.qualifiedName else coreTypes[type.value]!!.qualifiedName}",
                            |           isResultNullable = ${type.isNullable}
                            |       )
                            """.trimMargin()
                    }
                )
                .replace(
                    "%s2",
                    graphTrees.joinToString(",\n") {
                        "\t\t\"${it.rawNavGraphGenParams.name}\""
                    }
                )
        )
    }
}