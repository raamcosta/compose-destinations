package com.ramcosta.composedestinations.codegen.commons

import java.io.OutputStream

operator fun OutputStream.plusAssign(str: String) {
    write(str.toByteArray())
}

operator fun StringBuilder.plusAssign(str: String) {
    append(str)
}