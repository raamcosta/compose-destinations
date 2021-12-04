package com.ramcosta.composedestinations.codegen.writers

import com.ramcosta.composedestinations.codegen.commons.*
import com.ramcosta.composedestinations.codegen.facades.CodeOutputStreamMaker
import com.ramcosta.composedestinations.codegen.templates.*
import java.io.OutputStream

class CoreExtensionsWriter(
    private val codeGenerator: CodeOutputStreamMaker,
    private val generateNavGraphs: Boolean,
) {

    fun write() {
        val coreExtensions: OutputStream = codeGenerator.makeFile(
            packageName = PACKAGE_NAME,
            name = CORE_EXTENSIONS
        )

        var code = coreExtensionsTemplate

        code = if (generateNavGraphs) {
            code.removeInstancesOf(
                START_NO_NAV_GRAPHS_NAV_DESTINATION_ANCHOR,
                END_NO_NAV_GRAPHS_NAV_DESTINATION_ANCHOR,
                START_NAV_DESTINATION_ROOT_DEFAULT_ANCHOR,
                END_NAV_DESTINATION_ROOT_DEFAULT_ANCHOR
            )
        } else {
            code
                .removeFromTo(START_NO_NAV_GRAPHS_NAV_DESTINATION_ANCHOR, END_NO_NAV_GRAPHS_NAV_DESTINATION_ANCHOR)
                .removeFromTo(START_NAV_DESTINATION_ROOT_DEFAULT_ANCHOR, END_NAV_DESTINATION_ROOT_DEFAULT_ANCHOR)
        }

        coreExtensions += code
        coreExtensions.close()
    }
}