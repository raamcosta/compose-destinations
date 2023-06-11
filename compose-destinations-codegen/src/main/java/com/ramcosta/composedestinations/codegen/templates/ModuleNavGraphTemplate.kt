package com.ramcosta.composedestinations.codegen.templates

import com.ramcosta.composedestinations.codegen.codeGenBasePackageName
import com.ramcosta.composedestinations.codegen.commons.CORE_PACKAGE_NAME
import com.ramcosta.composedestinations.codegen.templates.core.FileTemplate
import com.ramcosta.composedestinations.codegen.templates.core.setOfImportable
import com.ramcosta.composedestinations.codegen.writers.sub.navGraphsPackageName

const val NAV_GRAPH_NAME_PLACEHOLDER = "@NAV_GRAPH_NAME_PLACEHOLDER@"
const val NAV_GRAPH_ROUTE_PLACEHOLDER = "@NAV_GRAPH_ROUTE_PLACEHOLDER@"
const val NAV_GRAPH_INVOKE_FUNCTION = "@NAV_GRAPH_INVOKE_FUNCTION@"
const val NAV_GRAPH_ARGS_FROM = "@NAV_GRAPH_ARGS_FROM@"
const val NAV_GRAPH_START_ROUTE_PLACEHOLDER = "@NAV_GRAPH_START_ROUTE_PLACEHOLDER@"
const val NAV_GRAPH_ARGUMENTS_PLACEHOLDER = "@NAV_GRAPH_ARGUMENTS_PLACEHOLDER@"
const val NAV_GRAPH_DEEP_LINKS_PLACEHOLDER = "@NAV_GRAPH_DEEP_LINKS_PLACEHOLDER@"
const val NAV_GRAPH_GEN_NAV_ARGS = "@NAV_GRAPH_GEN_NAV_ARGS@"
const val NAV_GRAPH_DESTINATIONS = "@NAV_GRAPH_DESTINATIONS@"
const val NESTED_NAV_GRAPHS = "@NESTED_NAV_GRAPHS@"
const val NAV_GRAPH_TYPE = "@NAV_GRAPH_TYPE@"
const val NAV_GRAPH_TYPED_ROUTE_TYPE = "@NAV_GRAPH_TYPED_ROUTE_TYPE@"
const val NAV_GRAPH_DEFAULT_TRANSITIONS_TYPE = "@NAV_GRAPH_DEFAULT_TRANSITIONS_TYPE@"
const val NAV_GRAPH_DEFAULT_TRANSITIONS = "@NAV_GRAPH_DEFAULT_TRANSITIONS@"

val moduleNavGraphTemplate = FileTemplate(
    packageStatement = "package $navGraphsPackageName",
    imports = setOfImportable(
        "$CORE_PACKAGE_NAME.spec.*",
        "${codeGenBasePackageName}.destinations.*",
    ),
    sourceCode = """
${REQUIRE_OPT_IN_ANNOTATIONS_PLACEHOLDER}public object $NAV_GRAPH_NAME_PLACEHOLDER : $NAV_GRAPH_TYPE {
    
    override val startRoute: TypedRoute<$NAV_GRAPH_TYPED_ROUTE_TYPE> = $NAV_GRAPH_START_ROUTE_PLACEHOLDER
    
    override val destinations: List<$typeAliasDestination> get() = listOf(
$NAV_GRAPH_DESTINATIONS
    )$NESTED_NAV_GRAPHS

	override val defaultTransitions: $NAV_GRAPH_DEFAULT_TRANSITIONS_TYPE = $NAV_GRAPH_DEFAULT_TRANSITIONS
    
$NAV_GRAPH_ROUTE_PLACEHOLDER$NAV_GRAPH_INVOKE_FUNCTION$NAV_GRAPH_ARGS_FROM$NAV_GRAPH_ARGUMENTS_PLACEHOLDER$NAV_GRAPH_DEEP_LINKS_PLACEHOLDER$NAV_GRAPH_GEN_NAV_ARGS
}

""".trimIndent()
)