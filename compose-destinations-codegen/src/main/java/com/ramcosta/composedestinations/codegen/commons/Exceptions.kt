package com.ramcosta.composedestinations.codegen.commons

class IllegalDestinationsSetup(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

class MissingRequiredDependency(message: String) : RuntimeException(message)