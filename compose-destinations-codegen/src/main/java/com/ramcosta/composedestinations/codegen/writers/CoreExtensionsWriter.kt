package com.ramcosta.composedestinations.codegen.writers

import com.ramcosta.composedestinations.codegen.commons.CORE_ANIMATION_EXTENSIONS
import com.ramcosta.composedestinations.codegen.commons.CORE_EXTENSIONS
import com.ramcosta.composedestinations.codegen.commons.PACKAGE_NAME
import com.ramcosta.composedestinations.codegen.commons.plusAssign
import com.ramcosta.composedestinations.codegen.facades.CodeOutputStreamMaker
import com.ramcosta.composedestinations.codegen.model.AvailableDependencies
import com.ramcosta.composedestinations.codegen.templates.coreAnimationsExtensionsTemplate
import com.ramcosta.composedestinations.codegen.templates.coreExtensionsTemplate
import java.io.OutputStream

class CoreExtensionsWriter(
    private val codeGenerator: CodeOutputStreamMaker,
    private val availableDependencies: AvailableDependencies
) {

    fun write() {
        val coreExtensions: OutputStream = codeGenerator.makeFile(
            packageName = PACKAGE_NAME,
            name = CORE_EXTENSIONS
        )

        coreExtensions += coreExtensionsTemplate

        coreExtensions.close()

        if (availableDependencies.accompanistAnimation) {
            val animationsExtension: OutputStream = codeGenerator.makeFile(
                packageName = PACKAGE_NAME,
                name = CORE_ANIMATION_EXTENSIONS
            )

            animationsExtension += coreAnimationsExtensionsTemplate

            animationsExtension.close()
        }
    }
}