package com.ramcosta.composedestinations.codegen.writers

import com.ramcosta.composedestinations.codegen.facades.CodeOutputStreamMaker
import com.ramcosta.composedestinations.codegen.templates.navtype.DEFAULT_KTX_SERIALIZABLE_NAV_TYPE_SERIALIZER_TEMPLATE_NAME
import com.ramcosta.composedestinations.codegen.templates.navtype.defaultKtxSerializableNavTypeSerializerTemplate
import com.ramcosta.composedestinations.codegen.templates.navtype.defaultKtxSerializableNavTypeSerializerTemplatePkg
import com.ramcosta.composedestinations.codegen.writers.helpers.writeSourceFile

class DefaultKtxSerializableNavTypeSerializerWriter(
    private val codeGenerator: CodeOutputStreamMaker,
) {
    fun write() {
        codeGenerator.makeFile(
            packageName = defaultKtxSerializableNavTypeSerializerTemplatePkg,
            name = DEFAULT_KTX_SERIALIZABLE_NAV_TYPE_SERIALIZER_TEMPLATE_NAME,
        ).writeSourceFile(defaultKtxSerializableNavTypeSerializerTemplate)
    }
}
