package com.ramcosta.composedestinations.codegen.templates

import com.ramcosta.composedestinations.codegen.codeGenBasePackageName
import com.ramcosta.composedestinations.codegen.templates.core.FileTemplate
import com.ramcosta.composedestinations.codegen.templates.core.setOfImportable

const val NAV_ARGS_METHOD_WHEN_CASES = "[NAV_ARGS_METHOD_WHEN_CASES]"
private const val CLASS_ESCAPED = "\${argsClass}"

val navArgsGettersTemplate = FileTemplate(
    packageStatement = "package $codeGenBasePackageName",
    imports = setOfImportable(
        "androidx.lifecycle.SavedStateHandle",
        "androidx.navigation.NavBackStackEntry"
    ),
    sourceCode = """
${REQUIRE_OPT_IN_ANNOTATIONS_PLACEHOLDER}public inline fun <reified T> SavedStateHandle.navArgs(): T {
    return navArgs(T::class.java, this)
}

${REQUIRE_OPT_IN_ANNOTATIONS_PLACEHOLDER}public inline fun <reified T> NavBackStackEntry.navArgs(): T {
    return navArgs(T::class.java, this)
}

@Suppress("UNCHECKED_CAST")
${REQUIRE_OPT_IN_ANNOTATIONS_PLACEHOLDER}public fun <T> navArgs(argsClass: Class<T>, argsContainer: SavedStateHandle): T {
    return when (argsClass) {
$NAV_ARGS_METHOD_WHEN_CASES
        else -> error("Class $CLASS_ESCAPED is not a navigation arguments class known by this module!")
    }
}

@Suppress("UNCHECKED_CAST")
${REQUIRE_OPT_IN_ANNOTATIONS_PLACEHOLDER}public fun <T> navArgs(argsClass: Class<T>, argsContainer: NavBackStackEntry): T {
    return when (argsClass) {
$NAV_ARGS_METHOD_WHEN_CASES
        else -> error("Class $CLASS_ESCAPED is not a navigation arguments class known by this module!")
    }
}

""".trimIndent()
)