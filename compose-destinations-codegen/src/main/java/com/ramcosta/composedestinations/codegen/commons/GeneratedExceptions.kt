package com.ramcosta.composedestinations.codegen.commons

object GeneratedExceptions {
    fun missingMandatoryArgument(argName: String) = "throw RuntimeException(\"'${argName}' argument is mandatory, but was not present!\")"
}
