package com.ramcosta.composedestinations.codegen.writers

import com.ramcosta.composedestinations.codegen.codeGenBasePackageName
import com.ramcosta.composedestinations.codegen.commons.removeFromTo
import com.ramcosta.composedestinations.codegen.commons.removeInstancesOf
import com.ramcosta.composedestinations.codegen.facades.CodeOutputStreamMaker
import com.ramcosta.composedestinations.codegen.templates.REGION_ACTIVITY_DESTINATION_END
import com.ramcosta.composedestinations.codegen.templates.REGION_ACTIVITY_DESTINATION_START
import com.ramcosta.composedestinations.codegen.templates.sealedDestinationTemplate
import com.ramcosta.composedestinations.codegen.writers.helpers.ImportableHelper
import com.ramcosta.composedestinations.codegen.writers.helpers.writeSourceFile

class SealedDestinationWriter(
    private val codeGenerator: CodeOutputStreamMaker
) {

    fun write(isThereAnyActivityDestination: Boolean) {
        val sourceCode = if (isThereAnyActivityDestination) {
            sealedDestinationTemplate.sourceCode
                .removeInstancesOf(REGION_ACTIVITY_DESTINATION_START, REGION_ACTIVITY_DESTINATION_END)
        } else {
            sealedDestinationTemplate.sourceCode
                .removeFromTo(REGION_ACTIVITY_DESTINATION_START, REGION_ACTIVITY_DESTINATION_END)
        }

        codeGenerator.makeFile(
            packageName = "$codeGenBasePackageName.destinations",
            name = "Destination"
        ).writeSourceFile(
            packageStatement = sealedDestinationTemplate.packageStatement,
            importableHelper = ImportableHelper(sealedDestinationTemplate.imports),
            sourceCode = sourceCode
        )
    }
}