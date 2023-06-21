package com.ramcosta.composedestinations.codegen.templates

import com.ramcosta.composedestinations.codegen.codeGenBasePackageName
import com.ramcosta.composedestinations.codegen.commons.CORE_PACKAGE_NAME
import com.ramcosta.composedestinations.codegen.commons.CORE_TYPED_DESTINATION_SPEC
import com.ramcosta.composedestinations.codegen.templates.core.FileTemplate
import com.ramcosta.composedestinations.codegen.templates.core.setOfImportable

const val MODULE_DESTINATIONS_PLACEHOLDER = "[MODULE_DESTINATIONS_PLACEHOLDER]"
const val MODULE_DESTINATIONS_LIST_NAME_PLACEHOLDER = "[MODULE_DESTINATIONS_LIST_NAME_PLACEHOLDER]"

val moduleDestinationTemplate = FileTemplate(
    packageStatement = "package $codeGenBasePackageName",
    imports = setOfImportable(
        "$CORE_PACKAGE_NAME.spec.*",
        "${codeGenBasePackageName}.destinations.*"
    ),
    sourceCode = """
${REQUIRE_OPT_IN_ANNOTATIONS_PLACEHOLDER}public val $MODULE_DESTINATIONS_LIST_NAME_PLACEHOLDER: List<$CORE_TYPED_DESTINATION_SPEC<out Any>> = listOf(
$MODULE_DESTINATIONS_PLACEHOLDER
)

""".trimIndent()
)