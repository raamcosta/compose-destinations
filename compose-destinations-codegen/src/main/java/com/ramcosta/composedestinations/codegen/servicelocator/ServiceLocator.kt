package com.ramcosta.composedestinations.codegen.servicelocator

import com.ramcosta.composedestinations.codegen.facades.CodeOutputStreamMaker
import com.ramcosta.composedestinations.codegen.model.CodeGenConfig
import com.ramcosta.composedestinations.codegen.model.CustomNavType
import com.ramcosta.composedestinations.codegen.model.SubModuleInfo
import com.ramcosta.composedestinations.codegen.model.Type
import com.ramcosta.composedestinations.codegen.validators.InitialValidator
import com.ramcosta.composedestinations.codegen.writers.ArgsToSavedStateHandleUtilsWriter
import com.ramcosta.composedestinations.codegen.writers.CustomNavTypesWriter
import com.ramcosta.composedestinations.codegen.writers.DefaultKtxSerializableNavTypeSerializerWriter
import com.ramcosta.composedestinations.codegen.writers.DestinationsWriter
import com.ramcosta.composedestinations.codegen.writers.MermaidGraphWriter
import com.ramcosta.composedestinations.codegen.writers.ModuleOutputWriter
import com.ramcosta.composedestinations.codegen.writers.ModuleRegistryWriter
import com.ramcosta.composedestinations.codegen.writers.NavArgsGettersWriter
import com.ramcosta.composedestinations.codegen.writers.sub.DestinationsModeWriter
import com.ramcosta.composedestinations.codegen.writers.sub.NavGraphsSingleObjectWriter
import com.ramcosta.composedestinations.codegen.writers.sub.SingleNavGraphWriter

internal interface ServiceLocator {
    val codeGenerator: CodeOutputStreamMaker
    val codeGenConfig: CodeGenConfig
}

internal fun ServiceLocator.moduleOutputWriter(
    customNavTypeByType: Map<Type, CustomNavType>,
    submodules: List<SubModuleInfo>
) = ModuleOutputWriter(
    codeGenConfig,
    destinationsListModeWriter,
    navGraphsSingleObjectWriter(customNavTypeByType),
    navArgsGetters,
    argsToSavedStateHandle(customNavTypeByType, submodules),
    mermaidGraphWriter,
    moduleRegistryWriter(customNavTypeByType),
)

internal fun ServiceLocator.moduleRegistryWriter(
    customNavTypeByType: Map<Type, CustomNavType>,
) = ModuleRegistryWriter(
    customNavTypeByType,
    codeGenConfig,
    codeGenerator,
)

internal val ServiceLocator.mermaidGraphWriter get() = MermaidGraphWriter(
    codeGenConfig
)

internal val ServiceLocator.customNavTypeWriter get() = CustomNavTypesWriter(
    codeGenerator,
)

internal fun ServiceLocator.destinationsWriter(
    customNavTypeByType: Map<Type, CustomNavType>,
    submodules: List<SubModuleInfo>
) = DestinationsWriter(
    codeGenConfig,
    codeGenerator,
    customNavTypeByType,
    submodules,
)

internal val ServiceLocator.destinationsListModeWriter get() = DestinationsModeWriter(
    codeGenerator,
)

internal fun ServiceLocator.navGraphsSingleObjectWriter(
    customNavTypeByType: Map<Type, CustomNavType>
) = NavGraphsSingleObjectWriter(
    codeGenerator,
    customNavTypeByType,
    ::SingleNavGraphWriter
)

internal val ServiceLocator.initialValidator get() = InitialValidator(
    codeGenConfig
)

internal val ServiceLocator.defaultKtxSerializableNavTypeSerializerWriter get() =
    DefaultKtxSerializableNavTypeSerializerWriter(
        codeGenerator,
    )

internal val ServiceLocator.navArgsGetters get() =
    NavArgsGettersWriter(
        codeGenerator,
    )

internal fun ServiceLocator.argsToSavedStateHandle(
    customNavTypeByType: Map<Type, CustomNavType>,
    submodules: List<SubModuleInfo>
) = ArgsToSavedStateHandleUtilsWriter(
    codeGenerator,
    submodules,
    customNavTypeByType
)
