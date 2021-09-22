package com.ramcosta.composedestinations.codegen.facades

import java.io.OutputStream

interface CodeOutputStreamMaker {

    fun makeFile(
        name: String,
        packageName: String,
        vararg sourceIds: String,
    ): OutputStream
}