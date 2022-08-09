package com.ramcosta.composedestinations.codegen.templates

import com.ramcosta.composedestinations.codegen.codeGenBasePackageName
import com.ramcosta.composedestinations.codegen.commons.CORE_DIRECTION_NAV_GRAPH_SPEC
import com.ramcosta.composedestinations.codegen.commons.CORE_NAV_GRAPH_SPEC
import com.ramcosta.composedestinations.codegen.commons.CORE_PACKAGE_NAME
import com.ramcosta.composedestinations.codegen.commons.CORE_TYPED_NAV_GRAPH_SPEC
import com.ramcosta.composedestinations.codegen.templates.core.FileTemplate
import com.ramcosta.composedestinations.codegen.templates.core.setOfImportable
import com.ramcosta.composedestinations.codegen.writers.sub.navGraphsPackageName

const val NAV_GRAPH_NAME_PLACEHOLDER = "[NAV_GRAPH_NAME_PLACEHOLDER]"
const val NAV_GRAPH_ROUTE_PLACEHOLDER = "[NAV_GRAPH_ROUTE_PLACEHOLDER]"
const val NAV_GRAPH_START_ROUTE_PLACEHOLDER = "[NAV_GRAPH_START_ROUTE_PLACEHOLDER]"
const val NAV_GRAPH_DESTINATIONS = "[NAV_GRAPH_DESTINATIONS]"
const val NESTED_NAV_GRAPHS = "[NESTED_NAV_GRAPHS]"
const val NAV_GRAPH_TYPE = "[NAV_GRAPH_TYPE]"
const val NAV_GRAPH_DESTINATIONS_FIELD_NAME = "[NAV_GRAPH_DESTINATIONS_FIELD_NAME]"
const val NAV_GRAPH_DESTINATIONS_FIELD_ASSOCIATE_BY = "[NAV_GRAPH_DESTINATIONS_FIELD_ASSOCIATE_BY]"

val moduleNavGraphTemplate = FileTemplate(
    packageStatement = "package $navGraphsPackageName",
    imports = setOfImportable(
        "$CORE_PACKAGE_NAME.spec.$CORE_NAV_GRAPH_SPEC",
        "$CORE_PACKAGE_NAME.spec.$CORE_TYPED_NAV_GRAPH_SPEC",
        "$CORE_PACKAGE_NAME.spec.$CORE_DIRECTION_NAV_GRAPH_SPEC",
        "${codeGenBasePackageName}.destinations.*",
    ),
    sourceCode = """
${REQUIRE_OPT_IN_ANNOTATIONS_PLACEHOLDER}object $NAV_GRAPH_NAME_PLACEHOLDER : $NAV_GRAPH_TYPE {
    
    override val route = $NAV_GRAPH_ROUTE_PLACEHOLDER
    
    override val startRoute = $NAV_GRAPH_START_ROUTE_PLACEHOLDER
    
    override val $NAV_GRAPH_DESTINATIONS_FIELD_NAME get() = listOf(
$NAV_GRAPH_DESTINATIONS
    )$NAV_GRAPH_DESTINATIONS_FIELD_ASSOCIATE_BY
$NESTED_NAV_GRAPHS
}

""".trimIndent()
)