package com.ramcosta.composedestinations.codegen.writers

import com.ramcosta.composedestinations.codegen.codeGenBasePackageName
import com.ramcosta.composedestinations.codegen.commons.CORE_PACKAGE_NAME
import com.ramcosta.composedestinations.codegen.commons.RESULT_BACK_NAVIGATOR_QUALIFIED_NAME
import com.ramcosta.composedestinations.codegen.commons.RawNavGraphTree
import com.ramcosta.composedestinations.codegen.commons.coreTypes
import com.ramcosta.composedestinations.codegen.commons.isCustomTypeNavArg
import com.ramcosta.composedestinations.codegen.commons.sourceIds
import com.ramcosta.composedestinations.codegen.facades.CodeOutputStreamMaker
import com.ramcosta.composedestinations.codegen.model.CodeGenConfig
import com.ramcosta.composedestinations.codegen.model.CodeGenProcessedDestination
import com.ramcosta.composedestinations.codegen.model.CustomNavType
import com.ramcosta.composedestinations.codegen.model.Type
import com.ramcosta.composedestinations.codegen.model.TypeArgument
import com.ramcosta.composedestinations.codegen.model.TypeInfo
import com.ramcosta.composedestinations.codegen.model.Visibility
import com.ramcosta.composedestinations.codegen.moduleName
import com.ramcosta.composedestinations.codegen.registryId
import com.ramcosta.composedestinations.codegen.templates.core.setOfImportable
import com.ramcosta.composedestinations.codegen.writers.helpers.ImportableHelper
import com.ramcosta.composedestinations.codegen.writers.helpers.writeSourceFile

internal class ModuleRegistryWriter(
    private val customNavTypeByType: Map<Type, CustomNavType>,
    private val codeGenConfig: CodeGenConfig,
    private val codeGenerator: CodeOutputStreamMaker
) {
    companion object {

        private const val packageName = "_generated._ramcosta._composedestinations._moduleregistry"

    }

    fun write(
        destinations: List<CodeGenProcessedDestination>,
        graphTrees: List<RawNavGraphTree>
    ) {
        val resultBackTypesByDestination: List<Pair<CodeGenProcessedDestination, TypeInfo>> =
            destinations.mapNotNull { destination ->
                if (destination.visibility != Visibility.PUBLIC) return@mapNotNull null

                destination.parameters.firstOrNull { it.type.importable.qualifiedName == RESULT_BACK_NAVIGATOR_QUALIFIED_NAME }
                    ?.let {
                        val type =
                            (it.type.typeArguments.firstOrNull<TypeArgument>() as? TypeArgument.Typed)?.type
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
            packageName,
            sourceIds = sourceIds(destinations, graphTrees).toTypedArray()
        ).writeSourceFile(
            packageStatement = "package $packageName",
            importableHelper = importableHelper,
            sourceCode = """
                    public annotation class _Info_$registryId(
                        val moduleName: String,
                        val packageName: String,
                        val hasNavArgsPackage: Boolean,
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
                        moduleName = "$moduleName",
                        packageName = "$codeGenBasePackageName",
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