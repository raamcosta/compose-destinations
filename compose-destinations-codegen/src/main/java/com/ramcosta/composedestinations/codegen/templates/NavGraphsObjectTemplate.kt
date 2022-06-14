package com.ramcosta.composedestinations.codegen.templates

import com.ramcosta.composedestinations.codegen.codeGenBasePackageName
import com.ramcosta.composedestinations.codegen.codeGenDestination
import com.ramcosta.composedestinations.codegen.commons.CORE_PACKAGE_NAME
import com.ramcosta.composedestinations.codegen.commons.GENERATED_NAV_GRAPH
import com.ramcosta.composedestinations.codegen.commons.GENERATED_NAV_GRAPHS_OBJECT
import com.ramcosta.composedestinations.codegen.templates.core.FileTemplate
import com.ramcosta.composedestinations.codegen.templates.core.setOfImportable

const val NAV_GRAPHS_PLACEHOLDER = "[NAV_GRAPHS_PLACEHOLDER]"

val navGraphsObjectTemplate = FileTemplate(
    packageStatement = "package $codeGenBasePackageName",
    imports = setOfImportable(
        "${codeGenBasePackageName}.destinations.*",
        "$CORE_PACKAGE_NAME.spec.*",
    ),
    sourceCode = """
/**
 * Class generated if any Composable is annotated with `@Destination`.
 * It aggregates all [$codeGenDestination]s in their [$GENERATED_NAV_GRAPH]s.
 */
object $GENERATED_NAV_GRAPHS_OBJECT {

$NAV_GRAPHS_PLACEHOLDER
}
""".trimIndent()
)