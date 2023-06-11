@file:Suppress("ObjectPropertyName")

package com.ramcosta.composedestinations.codegen

import com.ramcosta.composedestinations.codegen.commons.*
import com.ramcosta.composedestinations.codegen.facades.CodeOutputStreamMaker
import com.ramcosta.composedestinations.codegen.model.*
import com.ramcosta.composedestinations.codegen.servicelocator.*
import java.util.*

private var _generatedDestination: String? = null
private var _generatedNoArgsDestination: String? = null
private var _generatedActivityDestination: String? = null
private var _generatedNoArgsActivityDestination: String? = null

private var _generatedTypedNavGraph: String? = null
private var _generatedDirectionNavGraph: String? = null
private var _generatedNavHostNavGraph: String? = null

internal lateinit var codeGenBasePackageName: String
internal lateinit var moduleName: String
internal val codeGenDestination get() = _generatedDestination ?: CORE_DESTINATION_SPEC
internal val codeGenNoArgsDestination get() = _generatedNoArgsDestination ?: CORE_DIRECTION_DESTINATION_SPEC
internal val codeGenActivityDestination get() = _generatedActivityDestination ?: CORE_ACTIVITY_DESTINATION_SPEC.simpleName
internal val codeGenNoArgsActivityDestination get() = _generatedNoArgsActivityDestination ?: CORE_DIRECTION_ACTIVITY_DESTINATION_SPEC.simpleName

internal val codeGenTypedNavGraph: String get() = _generatedTypedNavGraph ?: CORE_TYPED_NAV_GRAPH_SPEC.simpleName
internal val codeGenDirectionNavGraph: String get() = _generatedDirectionNavGraph ?: CORE_DIRECTION_NAV_GRAPH_SPEC.simpleName
internal val codeGenNavHostNavGraph: String get() = _generatedNavHostNavGraph ?: CORE_NAV_HOST_GRAPH_SPEC.simpleName

class CodeGenerator(
    override val codeGenerator: CodeOutputStreamMaker,
    override val isBottomSheetDependencyPresent: Boolean,
    override val codeGenConfig: CodeGenConfig
) : ServiceLocatorAccessor {

    fun generate(
        destinations: List<RawDestinationGenParams>,
        navGraphs: List<RawNavGraphGenParams>,
        navTypeSerializers: List<NavTypeSerializer>
    ) {
        initialValidator.validate(navGraphs, destinations)

        initConfigurationValues(destinations)

        val destinationsWithNavArgs = destinationWithNavArgsMapper.map(destinations)

        val navTypeNamesByType = customNavTypeWriter.write(destinationsWithNavArgs, navTypeSerializers)

        val generatedDestinations = destinationsWriter(navTypeNamesByType)
            .write(destinationsWithNavArgs)

        moduleOutputWriter(navTypeNamesByType).write(navGraphs, generatedDestinations)

        sealedDestinationWriter.write(destinationsWithNavArgs.any { it.activityDestinationParams != null })

        if (shouldWriteKtxSerializableNavTypeSerializer(destinationsWithNavArgs)) {
            defaultKtxSerializableNavTypeSerializerWriter.write()
        }

        navArgsGetters.write(generatedDestinations)
    }

    private fun initConfigurationValues(
        destinations: List<DestinationGeneratingParams>
    ) {
        codeGenBasePackageName = (codeGenConfig.packageName ?: destinations.getCommonPackageNamePart()).sanitizePackageName()
        moduleName = codeGenConfig.moduleName?.replaceFirstChar { it.uppercase(Locale.US) } ?: ""

        _generatedDestination = moduleName + NO_PREFIX_GENERATED_DESTINATION
        _generatedNoArgsDestination = moduleName + NO_PREFIX_GENERATED_NO_ARGS_DESTINATION
        _generatedActivityDestination = moduleName + NO_PREFIX_GENERATED_ACTIVITY_DESTINATION
        _generatedNoArgsActivityDestination = moduleName + NO_PREFIX_GENERATED_NO_ARGS_ACTIVITY_DESTINATION

        _generatedTypedNavGraph = moduleName + NO_PREFIX_GENERATED_TYPED_NAV_GRAPH
        _generatedDirectionNavGraph = moduleName + NO_PREFIX_GENERATED_DIRECTION_NAV_GRAPH
        _generatedNavHostNavGraph = moduleName + NO_PREFIX_GENERATED_NAV_HOST_GRAPH
    }

    private fun shouldWriteKtxSerializableNavTypeSerializer(
        destinations: List<DestinationGeneratingParamsWithNavArgs>,
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

    private fun List<DestinationGeneratingParams>.getCommonPackageNamePart(): String {
        var currentCommonPackageName = ""
        map { it.composableQualifiedName }
            .forEachIndexed { idx, packageName ->
                if (idx == 0) {
                    currentCommonPackageName = packageName
                    return@forEachIndexed
                }
                currentCommonPackageName = currentCommonPackageName.commonPrefixWith(packageName)
            }

        if (!currentCommonPackageName.endsWith(".")) {
            currentCommonPackageName = currentCommonPackageName.split(".")
                .dropLast(1)
                .joinToString(".")
        }

        return currentCommonPackageName.removeSuffix(".")
            .ifEmpty {
                throw UnexpectedException(
                    """Unable to get package name for module. Please specify a package name to use in the module's build.gradle file with:"
                    ksp {
                        arg("compose-destinations.codeGenPackageName", "your.preferred.package.name")
                    }
                    And report this issue (with steps to reproduce) if possible. 
                """.trimIndent()
                )
            }
    }
}
