package com.ramcosta.composedestinations.codegen.templates

import com.ramcosta.composedestinations.codegen.codeGenBasePackageName
import com.ramcosta.composedestinations.codegen.commons.CORE_ALIAS_DESTINATION_SPEC
import com.ramcosta.composedestinations.codegen.commons.CORE_ALIAS_NAV_GRAPH_SPEC
import com.ramcosta.composedestinations.codegen.commons.CORE_PACKAGE_NAME
import com.ramcosta.composedestinations.codegen.commons.GENERATED_NAV_GRAPHS_OBJECT
import com.ramcosta.composedestinations.codegen.moduleName
import com.ramcosta.composedestinations.codegen.templates.core.FileTemplate
import com.ramcosta.composedestinations.codegen.templates.core.setOfImportable

const val NAV_GRAPHS_PLACEHOLDER = "[NAV_GRAPHS_PLACEHOLDER]"
const val NAV_GRAPHS_PRETTY_KDOC_PLACEHOLDER = "[NAV_GRAPHS_PRETTY_KDOC_PLACEHOLDER]"

val navGraphsObjectTemplate = FileTemplate(
    packageStatement = "package $codeGenBasePackageName",
    imports = setOfImportable(
        "${codeGenBasePackageName}.destinations.*",
        "${codeGenBasePackageName}.navgraphs.*",
        "$CORE_PACKAGE_NAME.spec.*",
    ),
    sourceCode = """
/**
 * Class generated if any Composable is annotated with `@Destination`.
 * It aggregates all [$CORE_ALIAS_DESTINATION_SPEC]s in their [$CORE_ALIAS_NAV_GRAPH_SPEC]s.$NAV_GRAPHS_PRETTY_KDOC_PLACEHOLDER
 */
internal object $moduleName$GENERATED_NAV_GRAPHS_OBJECT {

$NAV_GRAPHS_PLACEHOLDER
}
""".trimIndent()
)