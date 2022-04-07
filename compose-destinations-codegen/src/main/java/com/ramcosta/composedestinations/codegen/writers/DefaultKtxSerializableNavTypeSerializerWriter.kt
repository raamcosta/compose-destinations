package com.ramcosta.composedestinations.codegen.writers

import com.ramcosta.composedestinations.codegen.commons.plusAssign
import com.ramcosta.composedestinations.codegen.facades.CodeOutputStreamMaker
import com.ramcosta.composedestinations.codegen.templates.defaultKtxSerializableNavTypeSerializerTemplate
import com.ramcosta.composedestinations.codegen.templates.DEFAULT_KTX_SERIALIZABLE_NAV_TYPE_SERIALIZER_TEMPLATE_NAME
import com.ramcosta.composedestinations.codegen.templates.defaultKtxSerializableNavTypeSerializerTemplatePkg
import java.io.OutputStream

class DefaultKtxSerializableNavTypeSerializerWriter(
    private val codeGenerator: CodeOutputStreamMaker
) {
    fun write() {
        val file: OutputStream = codeGenerator.makeFile(
            packageName = defaultKtxSerializableNavTypeSerializerTemplatePkg,
            name = DEFAULT_KTX_SERIALIZABLE_NAV_TYPE_SERIALIZER_TEMPLATE_NAME,
        )

        file += defaultKtxSerializableNavTypeSerializerTemplate
        file.close()
    }
}
