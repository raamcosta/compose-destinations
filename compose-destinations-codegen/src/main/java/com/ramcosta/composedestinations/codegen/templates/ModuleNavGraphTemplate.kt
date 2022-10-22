package com.ramcosta.composedestinations.codegen.templates

import com.ramcosta.composedestinations.codegen.codeGenBasePackageName
import com.ramcosta.composedestinations.codegen.commons.CORE_DESTINATION_SPEC
import com.ramcosta.composedestinations.codegen.commons.CORE_NAV_GRAPH_SPEC
import com.ramcosta.composedestinations.codegen.commons.CORE_PACKAGE_NAME
import com.ramcosta.composedestinations.codegen.templates.core.FileTemplate
import com.ramcosta.composedestinations.codegen.templates.core.setOfImportable

const val NAV_GRAPH_NAME_PLACEHOLDER = "[NAV_GRAPH_NAME_PLACEHOLDER]"
const val NAV_GRAPH_ROUTE_PLACEHOLDER = "[NAV_GRAPH_ROUTE_PLACEHOLDER]"
const val NAV_GRAPH_START_ROUTE_PLACEHOLDER = "[NAV_GRAPH_START_ROUTE_PLACEHOLDER]"
const val NAV_GRAPH_DESTINATIONS = "[NAV_GRAPH_DESTINATIONS]"
const val NESTED_NAV_GRAPHS = "[NESTED_NAV_GRAPHS]"

val moduleNavGraphTemplate = FileTemplate(
    packageStatement = "package $codeGenBasePackageName",
    imports = setOfImportable(
        "$CORE_PACKAGE_NAME.spec.*",
        "${codeGenBasePackageName}.destinations.*",
    ),
    sourceCode = """
${REQUIRE_OPT_IN_ANNOTATIONS_PLACEHOLDER}public object $NAV_GRAPH_NAME_PLACEHOLDER : $CORE_NAV_GRAPH_SPEC {
    
    override val route: String = $NAV_GRAPH_ROUTE_PLACEHOLDER
    
    override val startRoute: Route = $NAV_GRAPH_START_ROUTE_PLACEHOLDER
    
    override val destinationsByRoute: Map<String, $CORE_DESTINATION_SPEC<*>> = listOf(
$NAV_GRAPH_DESTINATIONS
    ).associateBy { it.route }
$NESTED_NAV_GRAPHS
}

""".trimIndent()
)