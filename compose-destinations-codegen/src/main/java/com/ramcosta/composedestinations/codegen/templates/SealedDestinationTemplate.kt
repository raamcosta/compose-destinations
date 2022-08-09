package com.ramcosta.composedestinations.codegen.templates

import com.ramcosta.composedestinations.codegen.codeGenBasePackageName
import com.ramcosta.composedestinations.codegen.codeGenDestination
import com.ramcosta.composedestinations.codegen.codeGenNoArgsDestination
import com.ramcosta.composedestinations.codegen.commons.CORE_DIRECTION_DESTINATION_SPEC
import com.ramcosta.composedestinations.codegen.commons.CORE_PACKAGE_NAME
import com.ramcosta.composedestinations.codegen.commons.CORE_TYPED_DESTINATION_SPEC
import com.ramcosta.composedestinations.codegen.moduleName
import com.ramcosta.composedestinations.codegen.templates.core.FileTemplate
import com.ramcosta.composedestinations.codegen.templates.core.setOfImportable

val typeAliasDestination = "${moduleName}Destination"

val sealedDestinationTemplate = FileTemplate(
    packageStatement = "package $codeGenBasePackageName.destinations",
    imports = setOfImportable(
        "androidx.lifecycle.SavedStateHandle",
        "androidx.navigation.NavBackStackEntry",
        "$CORE_PACKAGE_NAME.spec.*",
    ),
    sourceCode = """
/**
 * Handy typealias of [$codeGenDestination] when you don't
 * care about the generic type (probably most cases for app's use)
 */
typealias $typeAliasDestination = $codeGenDestination<*>

/**
 * $codeGenDestination is a sealed version of [$CORE_TYPED_DESTINATION_SPEC]
 */
sealed interface $codeGenDestination<T>: $CORE_TYPED_DESTINATION_SPEC<T>

/**
 * $codeGenNoArgsDestination is a sealed version of [$CORE_DIRECTION_DESTINATION_SPEC]
 */
sealed interface $codeGenNoArgsDestination: $codeGenDestination<Unit>, $CORE_DIRECTION_DESTINATION_SPEC

""".trimIndent()
)