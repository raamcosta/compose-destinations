package com.ramcosta.composedestinations.codegen.templates

import com.ramcosta.composedestinations.codegen.codeGenBasePackageName
import com.ramcosta.composedestinations.codegen.commons.CORE_PACKAGE_NAME
import com.ramcosta.composedestinations.codegen.templates.core.FileTemplate
import com.ramcosta.composedestinations.codegen.templates.core.setOfImportable

const val MODULE_DESTINATIONS_PLACEHOLDER = "@MODULE_DESTINATIONS_PLACEHOLDER@"
const val MODULE_EXTERNAL_DESTINATIONS_PLACEHOLDER = "@MODULE_EXTERNAL_DESTINATIONS_PLACEHOLDER@"
const val MODULE_DESTINATIONS_CLASS_NAME_PLACEHOLDER = "@MODULE_DESTINATIONS_CLASS_NAME_PLACEHOLDER@"

val moduleDestinationTemplate = FileTemplate(
    packageStatement = "package $codeGenBasePackageName",
    imports = setOfImportable(
        "$CORE_PACKAGE_NAME.spec.*",
        "${codeGenBasePackageName}.destinations.*",
        "$CORE_PACKAGE_NAME.annotation.internal.GeneratedCodeExternalDestinations"
    ),
    sourceCode = """
${REQUIRE_OPT_IN_ANNOTATIONS_PLACEHOLDER}public data object $MODULE_DESTINATIONS_CLASS_NAME_PLACEHOLDER : ModuleDestinationsContainer {
  public override val destinations: List<DestinationSpec> = listOf<DestinationSpec>(
$MODULE_DESTINATIONS_PLACEHOLDER
    )
    
$MODULE_EXTERNAL_DESTINATIONS_PLACEHOLDER
  public object Includes
}

""".trimIndent()
)