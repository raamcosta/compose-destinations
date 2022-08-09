package com.ramcosta.composedestinations.codegen.servicelocator

import com.ramcosta.composedestinations.codegen.commons.DestinationWithNavArgsMapper
import com.ramcosta.composedestinations.codegen.facades.CodeOutputStreamMaker
import com.ramcosta.composedestinations.codegen.model.*
import com.ramcosta.composedestinations.codegen.validators.InitialValidator
import com.ramcosta.composedestinations.codegen.writers.*
import com.ramcosta.composedestinations.codegen.writers.sub.*

internal interface ServiceLocatorAccessor {
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
)

internal fun ServiceLocatorAccessor.destinationsWriter(
    customNavTypeByType: Map<Type, CustomNavType>
) = DestinationsWriter(
    codeGenerator,
    core,
    customNavTypeByType
)

internal val ServiceLocatorAccessor.destinationsListModeWriter get() = DestinationsModeWriter(
    codeGenerator,
)

internal val ServiceLocatorAccessor.sealedNavGraphWriter get() = SealedNavGraphWriter(
    codeGenerator
)

internal val ServiceLocatorAccessor.navGraphsModeWriter: NavGraphsModeWriter
    get() {
        return NavGraphsModeWriter(
            codeGenerator,
            codeGenConfig,
            sealedNavGraphWriter,
            ::SingleNavGraphWriter,
        )
    }

internal val ServiceLocatorAccessor.navGraphsSingleObjectWriter get() = NavGraphsSingleObjectWriter(
    codeGenerator,
    sealedNavGraphWriter,
    ::SingleNavGraphWriter
)

//region legacy navgraphs setup
internal val ServiceLocatorAccessor.legacyNavGraphsModeWriter get() = LegacyNavGraphsModeWriter(
    codeGenerator,
    codeGenConfig,
)

internal val ServiceLocatorAccessor.legacyNavGraphsSingleObjectWriter get() = LegacyNavGraphsSingleObjectWriter(
    codeGenerator,
)
//endregion

internal val ServiceLocatorAccessor.singleModuleExtensionsWriter get() = SingleModuleExtensionsWriter(
    codeGenerator,
)

internal val ServiceLocatorAccessor.sealedDestinationWriter get() = SealedDestinationWriter(
    codeGenerator,
)

internal val ServiceLocatorAccessor.destinationWithNavArgsMapper get() = DestinationWithNavArgsMapper()

internal val ServiceLocatorAccessor.initialValidator get() = InitialValidator(
    codeGenConfig,
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
