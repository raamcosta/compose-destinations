package com.ramcosta.composedestinations.codegen.templates

import com.ramcosta.composedestinations.codegen.codeGenBasePackageName

const val NAV_ARGS_METHOD_WHEN_CASES = "[NAV_ARGS_METHOD_WHEN_CASES]"
private const val CLASS_ESCAPED = "\${argsClass}"

val navArgsGettersTemplate = """
package $codeGenBasePackageName

import androidx.lifecycle.SavedStateHandle
$ADDITIONAL_IMPORTS

${REQUIRE_OPT_IN_ANNOTATIONS_PLACEHOLDER}inline fun <reified T> SavedStateHandle.navArgs(): T {
    return navArgs(T::class.java, this)
}

@Suppress("UNCHECKED_CAST")
${REQUIRE_OPT_IN_ANNOTATIONS_PLACEHOLDER}fun <T> navArgs(argsClass: Class<T>, savedStateHandle: SavedStateHandle): T {
    return when (argsClass) {
$NAV_ARGS_METHOD_WHEN_CASES
        else -> error("Class $CLASS_ESCAPED is not a navigation arguments class!")
    }
}

""".trimIndent()