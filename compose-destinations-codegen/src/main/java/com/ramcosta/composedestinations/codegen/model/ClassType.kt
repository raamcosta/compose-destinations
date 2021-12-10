package com.ramcosta.composedestinations.codegen.model

interface ClassType {
    val simpleName: String
    val qualifiedName: String

    companion object {
        operator fun invoke(
            simpleName: String,
            qualifiedName: String
        ): ClassType = ClassTypeImpl(
            simpleName,
            qualifiedName
        )
    }
}

data class ClassTypeImpl(
    override val simpleName: String,
    override val qualifiedName: String
) : ClassType