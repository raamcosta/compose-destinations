package com.ramcosta.composedestinations.codegen.writers

import com.ramcosta.composedestinations.codegen.codeGenBasePackageName
import com.ramcosta.composedestinations.codegen.commons.RESULT_BACK_NAVIGATOR_QUALIFIED_NAME
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
    fun write(destinations: List<CodeGenProcessedDestination>) {
        val resultBackTypesByDestination: List<Pair<CodeGenProcessedDestination, TypeInfo>> = destinations.mapNotNull { destination ->
            if (destination.visibility != Visibility.PUBLIC) return@mapNotNull null

            destination.parameters.firstOrNull { it.type.importable.qualifiedName == RESULT_BACK_NAVIGATOR_QUALIFIED_NAME }?.let {
                val type = (it.type.typeArguments.firstOrNull() as? TypeArgument.Typed)?.type
                    ?: return@mapNotNull null

                destination to type
            }
        }

        val registrySuffix = moduleName.ifEmpty { UUID.randomUUID().toString().replace("-", "_") }
        val importableHelper = ImportableHelper(
            setOfImportable(
                "com.ramcosta.composedestinations.spec.DestinationSpec",
                "kotlin.reflect.KClass"
            )
        )
        codeGenerator.makeFile(
            "_ModuleRegistry",
            "_generated._ramcosta._composedestinations._moduleregistry"
        ).writeSourceFile(
            packageStatement = "package _generated._ramcosta._composedestinations._moduleregistry",
            importableHelper = importableHelper,
            sourceCode = """
                    annotation class _Info_$registrySuffix(
                        val moduleName: String,
                        val packageName: String,
                        val typeResults: Array<_Destination_Result_Info$registrySuffix> = emptyArray()
                    )
                    
                    annotation class _Destination_Result_Info$registrySuffix(
                        val destination: KClass<out DestinationSpec>,
                        val resultType: KClass<*>,
                        val isResultNullable: Boolean
                    )
                    
                    @_Info_$registrySuffix(
                        moduleName = "${codeGenConfig.moduleName ?: ""}",
                        packageName = "$codeGenBasePackageName",
                        typeResults = [
                    %s1
                        ]
                    )
                    object _ModuleRegistry_$registrySuffix
                """.trimIndent()
                .replace(
                    "%s1",
                    resultBackTypesByDestination.joinToString(",\n") { (destination, type) ->
                        """
                        |       _Destination_Result_Info$registrySuffix(
                        |           destination = ${importableHelper.addAndGetPlaceholder(destination.destinationImportable)}::class,
                        |           resultType = ${importableHelper.addAndGetPlaceholder(type.importable)}::class,
                        |           isResultNullable = ${type.isNullable}
                        |       )
                        """.trimMargin()
                    }
                )
        )
    }
}