package com.ramcosta.composedestinations.codegen.commons

import java.io.OutputStream

internal operator fun OutputStream.plusAssign(str: String) {
    write(str.toByteArray())
}

internal operator fun StringBuilder.plusAssign(str: String) {
    append(str)
}