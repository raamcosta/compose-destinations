package com.ramcosta.composedestinations.codegen.templates

import com.ramcosta.composedestinations.codegen.codeGenBasePackageName
import com.ramcosta.composedestinations.codegen.templates.core.FileTemplate
import com.ramcosta.composedestinations.codegen.templates.core.setOfImportable

const val NAV_ARGS_TO_BUNDLE_WHEN_CASES = "[NAV_ARGS_TO_BUNDLE_WHEN_CASES]"

val navArgsToBundleTemplate = FileTemplate(
    packageStatement = "package $codeGenBasePackageName",
    imports = setOfImportable(
        "android.os.Bundle",
        "android.os.Parcelable"
    ),
    sourceCode = """
$NAV_ARGS_TO_BUNDLE_WHEN_CASES
""".trimIndent()
)