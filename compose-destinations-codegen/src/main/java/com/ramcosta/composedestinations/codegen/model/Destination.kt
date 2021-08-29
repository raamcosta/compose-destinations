package com.ramcosta.composedestinations.codegen.model

class Destination(
    val name: String,
    val qualifiedName: String,
    val composableName: String,
    val composableQualifiedName: String,
    val cleanRoute: String,
    val parameters: List<Parameter>,
)