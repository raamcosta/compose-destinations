package com.ramcosta.composedestinations.codegen.templates

import com.ramcosta.composedestinations.codegen.codeGenBasePackageName

const val MODULE_DESTINATIONS_PLACEHOLDER = "[MODULE_DESTINATIONS_PLACEHOLDER]"
const val MODULE_DESTINATIONS_LIST_NAME_PLACEHOLDER = "[MODULE_DESTINATIONS_LIST_NAME_PLACEHOLDER]"

val moduleDestinationTemplate = """
package $codeGenBasePackageName

import ${codeGenBasePackageName}.destinations.*$ADDITIONAL_IMPORTS

${REQUIRE_OPT_IN_ANNOTATIONS_PLACEHOLDER}val $MODULE_DESTINATIONS_LIST_NAME_PLACEHOLDER = listOf(
$MODULE_DESTINATIONS_PLACEHOLDER
)

""".trimIndent()