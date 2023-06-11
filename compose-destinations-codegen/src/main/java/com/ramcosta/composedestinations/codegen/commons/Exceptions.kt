package com.ramcosta.composedestinations.codegen.commons

class IllegalDestinationsSetup(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

class MissingRequiredDependency(message: String) : RuntimeException(message)

class UnexpectedException(message: String) : RuntimeException(message)

const val DOCS_WEBSITE = "https://composedestinations.rafaelcosta.xyz/"
const val DOCS_WEBSITE_MULTI_MODULE_CONFIGS = "${DOCS_WEBSITE}codegenconfigs#multi-module-configs"