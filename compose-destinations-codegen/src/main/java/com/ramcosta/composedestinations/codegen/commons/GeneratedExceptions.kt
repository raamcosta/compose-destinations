package com.ramcosta.composedestinations.codegen.commons

object GeneratedExceptions {
    fun missingMandatoryArgument(argName: String) = "throw RuntimeException(\"'${argName}' argument is mandatory, but was not present!\")"
    fun missingRequestedArgument(className: String, composableName: String) = "throw RuntimeException(\"$composableName composable: '${className}' argument was requested, but it is not present!\")"

    const val MISSING_VISIBILITY_SCOPE = "throw RuntimeException(\"'$ANIMATED_VISIBILITY_SCOPE_SIMPLE_NAME' was requested but we don't have it. Did you specify a $GENERATED_ANIMATED_DESTINATION_STYLE for this route?\")"
    const val MISSING_COLUMN_SCOPE = "throw RuntimeException(\"'$COLUMN_SCOPE_SIMPLE_NAME' was requested but we don't have it. Did you specify a DestinationStyle.BottomSheet for this route?\")"
}
