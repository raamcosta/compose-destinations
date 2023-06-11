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
import com.ramcosta.composedestinations.codegen.writers.SealedDestinationWriter
import com.ramcosta.composedestinations.codegen.writers.SealedNavGraphWriter
import com.ramcosta.composedestinations.codegen.writers.sub.DestinationsModeWriter
import com.ramcosta.composedestinations.codegen.writers.sub.NavGraphsModeWriter
import com.ramcosta.composedestinations.codegen.writers.sub.NavGraphsSingleObjectWriter
import com.ramcosta.composedestinations.codegen.writers.sub.SingleModuleExtensionsWriter
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
    navGraphsModeWriter(customNavTypeByType),
    destinationsListModeWriter,
    navGraphsSingleObjectWriter(customNavTypeByType),
    singleModuleExtensionsWriter
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

internal val ServiceLocatorAccessor.sealedNavGraphWriter get() = SealedNavGraphWriter(
    codeGenerator,
    codeGenConfig
)

internal fun ServiceLocatorAccessor.navGraphsModeWriter(
    customNavTypeByType: Map<Type, CustomNavType>
): NavGraphsModeWriter {
        return NavGraphsModeWriter(
            codeGenerator,
            codeGenConfig,
            sealedNavGraphWriter,
            customNavTypeByType,
            ::SingleNavGraphWriter,
        )
    }

internal fun ServiceLocatorAccessor.navGraphsSingleObjectWriter(
    customNavTypeByType: Map<Type, CustomNavType>
) = NavGraphsSingleObjectWriter(
    codeGenerator,
    sealedNavGraphWriter,
    customNavTypeByType,
    ::SingleNavGraphWriter
)

internal val ServiceLocatorAccessor.singleModuleExtensionsWriter get() = SingleModuleExtensionsWriter(
    codeGenerator,
)

internal val ServiceLocatorAccessor.sealedDestinationWriter get() = SealedDestinationWriter(
    codeGenerator,
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
