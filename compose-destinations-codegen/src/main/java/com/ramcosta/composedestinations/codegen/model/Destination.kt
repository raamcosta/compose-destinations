package com.ramcosta.composedestinations.codegen.model

class Destination(
    val name: String,
    val qualifiedName: String,
    val composableName: String,
    val composableQualifiedName: String,
    val cleanRoute: String,
    val navParameters: List<Parameter>,
    val navController: Parameter?,
    val navBackStackEntry: Parameter?
)