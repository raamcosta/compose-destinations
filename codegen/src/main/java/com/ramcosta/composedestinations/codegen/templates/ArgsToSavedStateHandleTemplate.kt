package com.ramcosta.composedestinations.codegen.templates

import com.ramcosta.composedestinations.codegen.codeGenBasePackageName
import com.ramcosta.composedestinations.codegen.commons.CORE_PACKAGE_NAME
import com.ramcosta.composedestinations.codegen.templates.core.FileTemplate
import com.ramcosta.composedestinations.codegen.templates.core.setOfImportable

const val VISIBILITY_PLACEHOLDER = "@VISIBILITY_PLACEHOLDER@"
const val ARGS_DATA_CLASS_SIMPLE_NAME = "@ARGS_DATA_CLASS_SIMPLE_NAME@"
const val NAV_TYPE_PUT_IN_BUNDLE_CALLS_PLACEHOLDER = "@NAV_TYPE_PUT_IN_BUNDLE_CALLS_PLACEHOLDER@"
const val ALL_ARGS_SAVED_STATE_HANDLE_FUNCTIONS_PLACEHOLDER = "@ALL_ARGS_SAVED_STATE_HANDLE_FUNCTIONS_PLACEHOLDER@"

val argsToSavedStateHandleTemplate = FileTemplate(
    packageStatement = "package $codeGenBasePackageName.navargs",
    imports = setOfImportable(
        "androidx.lifecycle.SavedStateHandle",
        "$CORE_PACKAGE_NAME.navargs.primitives.*",
        "$CORE_PACKAGE_NAME.navargs.primitives.array.*",
        "$CORE_PACKAGE_NAME.navargs.primitives.arraylist.*",
    ),
    sourceCode = """
$ALL_ARGS_SAVED_STATE_HANDLE_FUNCTIONS_PLACEHOLDER
""".trimIndent()
)
