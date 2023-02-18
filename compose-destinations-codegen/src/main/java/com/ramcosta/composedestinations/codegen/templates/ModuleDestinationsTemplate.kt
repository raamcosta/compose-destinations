package com.ramcosta.composedestinations.codegen.templates

import com.ramcosta.composedestinations.codegen.codeGenBasePackageName
import com.ramcosta.composedestinations.codegen.codeGenDestination
import com.ramcosta.composedestinations.codegen.templates.core.FileTemplate
import com.ramcosta.composedestinations.codegen.templates.core.setOfImportable

const val MODULE_DESTINATIONS_PLACEHOLDER = "[MODULE_DESTINATIONS_PLACEHOLDER]"
const val MODULE_DESTINATIONS_LIST_NAME_PLACEHOLDER = "[MODULE_DESTINATIONS_LIST_NAME_PLACEHOLDER]"

val moduleDestinationTemplate = FileTemplate(
    packageStatement = "package $codeGenBasePackageName",
    imports = setOfImportable(
        "${codeGenBasePackageName}.destinations.*"
    ),
    sourceCode = """
${REQUIRE_OPT_IN_ANNOTATIONS_PLACEHOLDER}public val $MODULE_DESTINATIONS_LIST_NAME_PLACEHOLDER: List<$codeGenDestination<out Any>> = listOf(
$MODULE_DESTINATIONS_PLACEHOLDER
)

""".trimIndent()
)