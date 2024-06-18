package com.ramcosta.composedestinations.codegen.templates

import com.ramcosta.composedestinations.codegen.codeGenBasePackageName
import com.ramcosta.composedestinations.codegen.commons.CORE_PACKAGE_NAME
import com.ramcosta.composedestinations.codegen.templates.core.FileTemplate
import com.ramcosta.composedestinations.codegen.templates.core.setOfImportable

const val NAV_ARGS_METHOD_WHEN_CASES = "[NAV_ARGS_METHOD_WHEN_CASES]"
const val NAV_GRAPH_ARGS_METHOD_WHEN_CASES = "[NAV_GRAPH_ARGS_METHOD_WHEN_CASES]"
const val INLINE_NAV_GRAPH_ARGS_METHODS_SECTION_START = "[INLINE_NAV_GRAPH_ARGS_METHODS_SECTION_START]"
const val INLINE_NAV_GRAPH_ARGS_METHODS_SECTION_END = "[INLINE_NAV_GRAPH_ARGS_METHODS_SECTION_END]"
const val NAV_GRAPH_ARGS_METHODS_SECTION_START = "[NAV_GRAPH_ARGS_METHODS_SECTION_START]"
const val NAV_GRAPH_ARGS_METHODS_SECTION_END = "[NAV_GRAPH_ARGS_METHODS_SECTION_END]"
const val INLINE_DESTINATION_ARGS_METHODS_SECTION_START = "[INLINE_DESTINATION_ARGS_METHODS_SECTION_START]"
const val INLINE_DESTINATION_ARGS_METHODS_SECTION_END = "[INLINE_DESTINATION_ARGS_METHODS_SECTION_END]"
const val DESTINATION_ARGS_METHODS_SECTION_START = "[DESTINATION_ARGS_METHODS_SECTION_START]"
const val DESTINATION_ARGS_METHODS_SECTION_END = "[DESTINATION_ARGS_METHODS_SECTION_END]"
private const val CLASS_ESCAPED = "\${argsClass}"

val navArgsGettersTemplate = FileTemplate(
    packageStatement = "@file:Suppress(\"UNCHECKED_CAST\")\n\npackage $codeGenBasePackageName.navargs",
    imports = setOfImportable(
        "androidx.lifecycle.SavedStateHandle",
        "androidx.navigation.NavBackStackEntry",
        "$CORE_PACKAGE_NAME.spec.DestinationSpec",
        "$CORE_PACKAGE_NAME.spec.NavGraphSpec",
        "kotlin.reflect.KClass"
    ),
    sourceCode = """
$INLINE_DESTINATION_ARGS_METHODS_SECTION_START${REQUIRE_OPT_IN_ANNOTATIONS_PLACEHOLDER}public inline fun <reified T: Any> SavedStateHandle.navArgs(): T {
    return navArgs(T::class, this)
}

${REQUIRE_OPT_IN_ANNOTATIONS_PLACEHOLDER}public inline fun <reified T: Any> NavBackStackEntry.navArgs(): T {
    return navArgs(T::class, this)
}

${REQUIRE_OPT_IN_ANNOTATIONS_PLACEHOLDER}public fun <T: Any> navArgs(argsClass: KClass<T>, argsContainer: NavBackStackEntry): T {
    return destinationWithArgsType(argsClass).argsFrom(argsContainer) as T
}

${REQUIRE_OPT_IN_ANNOTATIONS_PLACEHOLDER}public fun <T: Any> navArgs(argsClass: KClass<T>, argsContainer: SavedStateHandle): T {
    return destinationWithArgsType(argsClass).argsFrom(argsContainer) as T
}$INLINE_DESTINATION_ARGS_METHODS_SECTION_END

$INLINE_NAV_GRAPH_ARGS_METHODS_SECTION_START${REQUIRE_OPT_IN_ANNOTATIONS_PLACEHOLDER}public inline fun <reified T: Any> SavedStateHandle.navGraphArgs(): T? {
    return navGraphArgs(T::class, this)
}

${REQUIRE_OPT_IN_ANNOTATIONS_PLACEHOLDER}public inline fun <reified T: Any> NavBackStackEntry.navGraphArgs(): T? {
    return navGraphArgs(T::class, this)
}

${REQUIRE_OPT_IN_ANNOTATIONS_PLACEHOLDER}public inline fun <reified T: Any> SavedStateHandle.requireNavGraphArgs(): T {
    return requireNavGraphArgs(T::class, this)
}

${REQUIRE_OPT_IN_ANNOTATIONS_PLACEHOLDER}public inline fun <reified T: Any> NavBackStackEntry.requireNavGraphArgs(): T {
    return requireNavGraphArgs(T::class, this)
}

public fun <T: Any> navGraphArgs(argsClass: KClass<T>, argsContainer: SavedStateHandle): T? {
    return navGraphWithArgsType(argsClass).argsFrom(argsContainer) as T?
}

public fun <T: Any> navGraphArgs(argsClass: KClass<T>, argsContainer: NavBackStackEntry): T? {
    return navGraphWithArgsType(argsClass).argsFrom(argsContainer) as T?
}

public fun <T: Any> requireNavGraphArgs(argsClass: KClass<T>, argsContainer: SavedStateHandle): T {
    return navGraphWithArgsType(argsClass).requireGraphArgs(argsContainer) as T
}

public fun <T: Any> requireNavGraphArgs(argsClass: KClass<T>, argsContainer: NavBackStackEntry): T {
    return navGraphWithArgsType(argsClass).requireGraphArgs(argsContainer) as T
}$INLINE_NAV_GRAPH_ARGS_METHODS_SECTION_END

$DESTINATION_ARGS_METHODS_SECTION_START${REQUIRE_OPT_IN_ANNOTATIONS_PLACEHOLDER}private fun <T: Any> destinationWithArgsType(argsClass: KClass<T>): DestinationSpec {
    return when (argsClass) {
$NAV_ARGS_METHOD_WHEN_CASES
        else -> error("Class $CLASS_ESCAPED is not a destination arguments class known by this module!")
    }
}$DESTINATION_ARGS_METHODS_SECTION_END

${NAV_GRAPH_ARGS_METHODS_SECTION_START}${REQUIRE_OPT_IN_ANNOTATIONS_PLACEHOLDER}private fun <T: Any> navGraphWithArgsType(argsClass: KClass<T>): NavGraphSpec {
    return when (argsClass) {
$NAV_GRAPH_ARGS_METHOD_WHEN_CASES
        else -> error("Class $CLASS_ESCAPED is not a navigation graph arguments class known by this module!")
    }
}$NAV_GRAPH_ARGS_METHODS_SECTION_END

""".trimIndent()
)