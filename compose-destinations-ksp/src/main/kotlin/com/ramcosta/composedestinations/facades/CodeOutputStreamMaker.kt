package com.ramcosta.composedestinations.facades

import java.io.OutputStream

interface CodeOutputStreamMaker {

    fun makeFile(name: String, packageName: String): OutputStream
}