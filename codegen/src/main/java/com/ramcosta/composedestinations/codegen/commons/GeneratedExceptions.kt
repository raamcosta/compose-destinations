package com.ramcosta.composedestinations.codegen.commons

object GeneratedExceptions {
    fun missingMandatoryArgument(argName: String) = "throw RuntimeException(\"'${argName}' argument is mandatory, but was not present!\")"
    fun nonMandatoryNonNullableMissingArgument(argName: String) = "throw RuntimeException(\"'${argName}' argument is not mandatory and not nullable but was not present!\")"
}
