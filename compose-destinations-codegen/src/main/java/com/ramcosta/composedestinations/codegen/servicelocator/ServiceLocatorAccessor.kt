package com.ramcosta.composedestinations.codegen.servicelocator

import com.ramcosta.composedestinations.codegen.commons.DestinationWithNavArgsMapper
import com.ramcosta.composedestinations.codegen.facades.CodeOutputStreamMaker
import com.ramcosta.composedestinations.codegen.model.CodeGenConfig
import com.ramcosta.composedestinations.codegen.model.CustomNavType
import com.ramcosta.composedestinations.codegen.model.Type
import com.ramcosta.composedestinations.codegen.validators.InitialValidator
import com.ramcosta.composedestinations.codegen.writers.CustomNavTypesWriter
import com.ramcosta.composedestinations.codegen.writers.DefaultKtxSerializableNavTypeSerializerWriter
import com.ramcosta.composedestinations.codegen.writers.DestinationsWriter
import com.ramcosta.composedestinations.codegen.writers.ModuleOutputWriter
import com.ramcosta.composedestinations.codegen.writers.NavArgsGettersWriter
import com.ramcosta.composedestinations.codegen.writers.sub.DestinationsModeWriter
import com.ramcosta.composedestinations.codegen.writers.sub.NavGraphsSingleObjectWriter
import com.ramcosta.composedestinations.codegen.writers.sub.SingleNavGraphWriter

internal interface ServiceLocatorAccessor {
    val codeGenerator: CodeOutputStreamMaker
    val isBottomSheetDependencyPresent: Boolean
    val codeGenConfig: CodeGenConfig
}

internal fun ServiceLocatorAccessor.moduleOutputWriter(
    customNavTypeByType: Map<Type, CustomNavType>
) = ModuleOutputWriter(
    codeGenConfig,
    destinationsListModeWriter,
    navGraphsSingleObjectWriter(customNavTypeByType),
    navArgsGetters
)

internal val ServiceLocatorAccessor.customNavTypeWriter get() = CustomNavTypesWriter(
    codeGenerator,
)

internal fun ServiceLocatorAccessor.destinationsWriter(
    customNavTypeByType: Map<Type, CustomNavType>
) = DestinationsWriter(
    codeGenConfig,
    codeGenerator,
    isBottomSheetDependencyPresent,
    customNavTypeByType
)

internal val ServiceLocatorAccessor.destinationsListModeWriter get() = DestinationsModeWriter(
    codeGenerator,
)

internal fun ServiceLocatorAccessor.navGraphsSingleObjectWriter(
    customNavTypeByType: Map<Type, CustomNavType>
) = NavGraphsSingleObjectWriter(
    codeGenerator,
    customNavTypeByType,
    ::SingleNavGraphWriter
)

internal val ServiceLocatorAccessor.destinationWithNavArgsMapper get() = DestinationWithNavArgsMapper()

internal val ServiceLocatorAccessor.initialValidator get() = InitialValidator(
    codeGenConfig,
    isBottomSheetDependencyPresent
)

internal val ServiceLocatorAccessor.defaultKtxSerializableNavTypeSerializerWriter get() =
    DefaultKtxSerializableNavTypeSerializerWriter(
        codeGenerator,
    )

internal val ServiceLocatorAccessor.navArgsGetters get() =
    NavArgsGettersWriter(
        codeGenerator,
    )
