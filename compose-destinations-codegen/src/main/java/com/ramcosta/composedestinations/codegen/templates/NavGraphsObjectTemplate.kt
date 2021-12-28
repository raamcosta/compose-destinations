package com.ramcosta.composedestinations.codegen.templates

import com.ramcosta.composedestinations.codegen.commons.GENERATED_DESTINATION
import com.ramcosta.composedestinations.codegen.commons.GENERATED_NAV_GRAPH
import com.ramcosta.composedestinations.codegen.commons.GENERATED_NAV_GRAPHS_OBJECT
import com.ramcosta.composedestinations.codegen.commons.PACKAGE_NAME

const val NAV_GRAPHS_PLACEHOLDER = "[NAV_GRAPHS_PLACEHOLDER]"

val navGraphsObjectTemplate = """
package $PACKAGE_NAME

import ${PACKAGE_NAME}.destinations.*$ADDITIONAL_IMPORTS

/**
 * Class generated if any Composable is annotated with `@Destination`.
 * It aggregates all [$GENERATED_DESTINATION]s in their [$GENERATED_NAV_GRAPH]s.
 */
object $GENERATED_NAV_GRAPHS_OBJECT {

$NAV_GRAPHS_PLACEHOLDER
}
""".trimIndent()