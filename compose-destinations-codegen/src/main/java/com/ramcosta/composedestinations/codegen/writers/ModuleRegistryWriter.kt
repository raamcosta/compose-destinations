package com.ramcosta.composedestinations.codegen.writers

import com.ramcosta.composedestinations.codegen.codeGenBasePackageName
import com.ramcosta.composedestinations.codegen.commons.RESULT_BACK_NAVIGATOR_QUALIFIED_NAME
import com.ramcosta.composedestinations.codegen.commons.RawNavGraphTree
import com.ramcosta.composedestinations.codegen.commons.plusAssign
import com.ramcosta.composedestinations.codegen.facades.CodeOutputStreamMaker
import com.ramcosta.composedestinations.codegen.model.CodeGenConfig
import com.ramcosta.composedestinations.codegen.model.CodeGenProcessedDestination
import com.ramcosta.composedestinations.codegen.model.TypeArgument
import com.ramcosta.composedestinations.codegen.model.TypeInfo
import com.ramcosta.composedestinations.codegen.model.Visibility
import com.ramcosta.composedestinations.codegen.moduleName
import com.ramcosta.composedestinations.codegen.templates.core.setOfImportable
import com.ramcosta.composedestinations.codegen.writers.helpers.ImportableHelper
import com.ramcosta.composedestinations.codegen.writers.helpers.writeSourceFile
import java.util.UUID

internal class ModuleRegistryWriter(
    private val codeGenConfig: CodeGenConfig,
    private val codeGenerator: CodeOutputStreamMaker
) {
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
                            (it.type.typeArguments.firstOrNull() as? TypeArgument.Typed)?.type
                                ?: return@mapNotNull null

                        destination to type
                    }
            }

        val registryId = moduleName.ifEmpty { UUID.randomUUID().toString().replace("-", "_") }
        val importableHelper = ImportableHelper(
            setOfImportable(
                "com.ramcosta.composedestinations.spec.DestinationSpec"
            )
        )
        codeGenerator.makeFile(
            "_ModuleRegistry_$registryId",
            "_generated._ramcosta._composedestinations._moduleregistry"
        ).writeSourceFile(
            packageStatement = "package _generated._ramcosta._composedestinations._moduleregistry",
            importableHelper = importableHelper,
            sourceCode = """
                    annotation class _Info_$registryId(
                        val moduleName: String,
                        val packageName: String,
                        val typeResults: Array<_Destination_Result_Info_$registryId> = emptyArray(),
                        val topLevelGraphs: Array<String> = emptyArray()
                    )
                    
                    annotation class _Destination_Result_Info_$registryId(
                        val destination: String,
                        val resultType: String,
                        val isResultNullable: Boolean
                    )
                    
                    @_Info_$registryId(
                        moduleName = "${codeGenConfig.moduleName ?: ""}",
                        packageName = "$codeGenBasePackageName",
                        typeResults = [
                    %s1
                        ],
                        topLevelGraphs = [
                    %s2
                        ]
                    )
                    object _ModuleRegistry_$registryId
                """.trimIndent()
                .replace(
                    "%s1",
                    resultBackTypesByDestination.joinToString(",\n") { (destination, type) ->
                        """
                        |       _Destination_Result_Info_$registryId(
                        |           destination = "${destination.destinationImportable.qualifiedName}",
                        |           resultType = "${type.importable.qualifiedName}",
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

    companion object {
        fun generateModuleRegistryPathInfo(
            codeGenerator: CodeOutputStreamMaker,
            moduleRegistryPath: String,
            moduleRegistryId: String
        ) {
            codeGenerator.makeFile(
                "_PathInfo_ModuleRegistry_$moduleRegistryId",
                "_generated._ramcosta._composedestinations._moduleregistry"
            ).use {
                it += """
                    package _generated._ramcosta._composedestinations._moduleregistry
                    
                    annotation class _Annotation_PathInfo_$moduleRegistryId(
                        val path: String,
                        val moduleRegistryId: String
                    )
                    
                    @_Annotation_PathInfo_$moduleRegistryId(
                        path = "$moduleRegistryPath",
                        moduleRegistryId = "$moduleRegistryId"
                    )
                    object _PathInfo_ModuleRegistry_$moduleRegistryId
                """.trimIndent()
            }
        }
    }
}