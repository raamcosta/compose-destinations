package com.ramcosta.composedestinations.codegen.templates

import com.ramcosta.composedestinations.codegen.codeGenBasePackageName
import com.ramcosta.composedestinations.codegen.commons.*

const val NAV_GRAPH_NAME_PLACEHOLDER = "[NAV_GRAPH_NAME_PLACEHOLDER]"
const val NAV_GRAPH_ROUTE_PLACEHOLDER = "[NAV_GRAPH_ROUTE_PLACEHOLDER]"
const val NAV_GRAPH_START_ROUTE_PLACEHOLDER = "[NAV_GRAPH_START_ROUTE_PLACEHOLDER]"
const val NAV_GRAPH_DESTINATIONS = "[NAV_GRAPH_DESTINATIONS]"

val moduleNavGraphTemplate = """
package $codeGenBasePackageName

import $CORE_PACKAGE_NAME.spec.$CORE_NAV_GRAPH_SPEC
import ${codeGenBasePackageName}.destinations.*$ADDITIONAL_IMPORTS

${REQUIRE_OPT_IN_ANNOTATIONS_PLACEHOLDER}object $NAV_GRAPH_NAME_PLACEHOLDER : $CORE_NAV_GRAPH_SPEC {
    
    override val route = $NAV_GRAPH_ROUTE_PLACEHOLDER
    
    override val startRoute = $NAV_GRAPH_START_ROUTE_PLACEHOLDER
    
    override val destinationsByRoute = listOf(
$NAV_GRAPH_DESTINATIONS
    ).associateBy { it.route }
}

""".trimIndent()