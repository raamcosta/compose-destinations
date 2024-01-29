@file:Suppress("ObjectPropertyName")

package com.ramcosta.composedestinations.codegen

import com.ramcosta.composedestinations.codegen.commons.RESULT_BACK_NAVIGATOR_QUALIFIED_NAME
import com.ramcosta.composedestinations.codegen.commons.firstTypeInfoArg
import com.ramcosta.composedestinations.codegen.commons.isCustomArrayOrArrayListTypeNavArg
import com.ramcosta.composedestinations.codegen.commons.sanitizePackageName
import com.ramcosta.composedestinations.codegen.facades.CodeOutputStreamMaker
import com.ramcosta.composedestinations.codegen.model.CodeGenConfig
import com.ramcosta.composedestinations.codegen.model.CodeGenProcessedDestination
import com.ramcosta.composedestinations.codegen.model.NavTypeSerializer
import com.ramcosta.composedestinations.codegen.model.RawDestinationGenParams
import com.ramcosta.composedestinations.codegen.model.RawNavGraphGenParams
import com.ramcosta.composedestinations.codegen.model.SubModuleInfo
import com.ramcosta.composedestinations.codegen.model.TypeArgument
import com.ramcosta.composedestinations.codegen.model.TypeInfo
import com.ramcosta.composedestinations.codegen.model.Visibility
import com.ramcosta.composedestinations.codegen.servicelocator.ServiceLocatorAccessor
import com.ramcosta.composedestinations.codegen.servicelocator.customNavTypeWriter
import com.ramcosta.composedestinations.codegen.servicelocator.defaultKtxSerializableNavTypeSerializerWriter
import com.ramcosta.composedestinations.codegen.servicelocator.destinationWithNavArgsMapper
import com.ramcosta.composedestinations.codegen.servicelocator.destinationsWriter
import com.ramcosta.composedestinations.codegen.servicelocator.initialValidator
import com.ramcosta.composedestinations.codegen.servicelocator.moduleOutputWriter
import com.ramcosta.composedestinations.codegen.templates.core.setOfImportable
import com.ramcosta.composedestinations.codegen.writers.helpers.ImportableHelper
import com.ramcosta.composedestinations.codegen.writers.helpers.writeSourceFile
import java.util.Locale
import java.util.UUID

private const val DEFAULT_GEN_PACKAGE_NAME = "com.ramcosta.composedestinations.generated"
internal lateinit var codeGenBasePackageName: String
internal lateinit var moduleName: String

class CodeGenerator(
    override val codeGenerator: CodeOutputStreamMaker,
    override val isBottomSheetDependencyPresent: Boolean,
    override val codeGenConfig: CodeGenConfig
) : ServiceLocatorAccessor {

    fun generate(
        destinations: List<RawDestinationGenParams>,
        navGraphs: List<RawNavGraphGenParams>,
        navTypeSerializers: List<NavTypeSerializer>,
        submodules: List<SubModuleInfo>
    ) {
        initialValidator.validate(
            navGraphs = navGraphs,
            destinations = destinations,
            submoduleResultSenders = submodules
                .flatMap { it.publicResultSenders }
                .associateBy { it.genDestinationQualifiedName }
        )

        initConfigurationValues()

        val processedDestinations: List<CodeGenProcessedDestination> = destinationWithNavArgsMapper.map(destinations)

        val navTypeNamesByType = customNavTypeWriter.write(processedDestinations, navTypeSerializers)

        moduleOutputWriter(navTypeNamesByType, submodules).write(navGraphs, processedDestinations)

        destinationsWriter(navTypeNamesByType).write(processedDestinations)

        if (shouldWriteKtxSerializableNavTypeSerializer(processedDestinations)) {
            defaultKtxSerializableNavTypeSerializerWriter.write()
        }

        addSubmoduleRegistry(processedDestinations)
    }

    private fun addSubmoduleRegistry(destinations: List<CodeGenProcessedDestination>) {
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

    private fun initConfigurationValues() {
        moduleName = codeGenConfig.moduleName?.replaceFirstChar { it.uppercase(Locale.US) } ?: ""
        val defaultPackageName = if (moduleName.isEmpty()) {
            DEFAULT_GEN_PACKAGE_NAME
        } else {
            "$DEFAULT_GEN_PACKAGE_NAME.${moduleName.lowercase()}".sanitizePackageName()
        }
        codeGenBasePackageName = codeGenConfig.packageName?.sanitizePackageName() ?: defaultPackageName
    }

    private fun shouldWriteKtxSerializableNavTypeSerializer(
        destinations: List<CodeGenProcessedDestination>,
    ) = destinations.any {
        it.navArgs.any { navArg ->
            if (navArg.type.isCustomArrayOrArrayListTypeNavArg()) {
               navArg.type.value.firstTypeInfoArg.run {
                   isKtxSerializable &&
                           !hasCustomTypeSerializer &&
                           !isParcelable &&
                           !isSerializable
               }
            } else {
                navArg.type.run {
                    isKtxSerializable &&
                            !hasCustomTypeSerializer &&
                            !isParcelable &&
                            !isSerializable
                }
            }
        }
    }
}
