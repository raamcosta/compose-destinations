package com.ramcosta.composedestinations.codegen.templates

import com.ramcosta.composedestinations.codegen.codeGenBasePackageName
import com.ramcosta.composedestinations.codegen.commons.*
import com.ramcosta.composedestinations.codegen.codeGenDestination

const val NAV_GRAPHS_PLACEHOLDER = "[NAV_GRAPHS_PLACEHOLDER]"

val navGraphsObjectTemplate = """
package $codeGenBasePackageName

import ${codeGenBasePackageName}.destinations.*$ADDITIONAL_IMPORTS

/**
 * Class generated if any Composable is annotated with `@Destination`.
 * It aggregates all [$codeGenDestination]s in their [$GENERATED_NAV_GRAPH]s.
 */
object $GENERATED_NAV_GRAPHS_OBJECT {

$NAV_GRAPHS_PLACEHOLDER
}
""".trimIndent()