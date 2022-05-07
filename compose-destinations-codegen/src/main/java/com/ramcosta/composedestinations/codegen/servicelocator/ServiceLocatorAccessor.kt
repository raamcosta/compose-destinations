package com.ramcosta.composedestinations.codegen.servicelocator

import com.ramcosta.composedestinations.codegen.commons.DestinationWithNavArgsMapper
import com.ramcosta.composedestinations.codegen.facades.CodeOutputStreamMaker
import com.ramcosta.composedestinations.codegen.facades.Logger
import com.ramcosta.composedestinations.codegen.model.*
import com.ramcosta.composedestinations.codegen.validators.InitialValidator
import com.ramcosta.composedestinations.codegen.writers.*
import com.ramcosta.composedestinations.codegen.writers.sub.*

internal interface ServiceLocatorAccessor {
    val logger: Logger
    val codeGenerator: CodeOutputStreamMaker
    val core: Core
    val codeGenConfig: CodeGenConfig
}

internal val ServiceLocatorAccessor.moduleOutputWriter get() = ModuleOutputWriter(
    codeGenConfig,
    navGraphsModeWriter,
    legacyNavGraphsModeWriter,
    destinationsListModeWriter,
    navGraphsSingleObjectWriter,
    legacyNavGraphsSingleObjectWriter,
    singleModuleExtensionsWriter
)

internal val ServiceLocatorAccessor.customNavTypeWriter get() = CustomNavTypesWriter(
    codeGenerator,
    logger
)

internal fun ServiceLocatorAccessor.destinationsWriter(
    customNavTypeByType: Map<Type, CustomNavType>
) = DestinationsWriter(
    codeGenerator,
    logger,
    core,
    customNavTypeByType
)

internal val ServiceLocatorAccessor.destinationsListModeWriter get() = DestinationsModeWriter(
    codeGenerator,
)

internal val ServiceLocatorAccessor.navGraphsModeWriter get() = NavGraphsModeWriter(
    logger,
    codeGenerator,
    codeGenConfig
)

internal val ServiceLocatorAccessor.navGraphsSingleObjectWriter get() = NavGraphsSingleObjectWriter(
    codeGenerator,
    logger,
    codeGenConfig
)

//region legacy navgraphs setup
internal val ServiceLocatorAccessor.legacyNavGraphsModeWriter get() = LegacyNavGraphsModeWriter(
    codeGenerator,
    codeGenConfig,
)

internal val ServiceLocatorAccessor.legacyNavGraphsSingleObjectWriter get() = LegacyNavGraphsSingleObjectWriter(
    codeGenerator,
    logger,
)
//endregion

internal val ServiceLocatorAccessor.singleModuleExtensionsWriter get() = SingleModuleExtensionsWriter(
    codeGenerator,
    logger
)

internal val ServiceLocatorAccessor.sealedDestinationWriter get() = SealedDestinationWriter(
    codeGenerator,
)

internal val ServiceLocatorAccessor.destinationWithNavArgsMapper get() = DestinationWithNavArgsMapper()

internal val ServiceLocatorAccessor.initialValidator get() = InitialValidator(
    codeGenConfig,
    logger,
    core
)

internal val ServiceLocatorAccessor.defaultKtxSerializableNavTypeSerializerWriter get() =
    DefaultKtxSerializableNavTypeSerializerWriter(
        codeGenerator,
    )

internal val ServiceLocatorAccessor.navArgsGetters get() =
    NavArgsGettersWriter(
        codeGenerator,
    )
