package com.ramcosta.composedestinations.codegen.writers

import com.ramcosta.composedestinations.codegen.codeGenBasePackageName
import com.ramcosta.composedestinations.codegen.commons.plusAssign
import com.ramcosta.composedestinations.codegen.facades.CodeOutputStreamMaker
import com.ramcosta.composedestinations.codegen.model.DestinationGeneratingParamsWithNavArgs
import com.ramcosta.composedestinations.codegen.model.GeneratedDestination
import com.ramcosta.composedestinations.codegen.model.TypeArgument
import com.ramcosta.composedestinations.codegen.templates.*
import com.ramcosta.composedestinations.codegen.writers.helpers.ImportableHelper
import com.ramcosta.composedestinations.codegen.writers.helpers.writeSourceFile
import java.io.OutputStream
import java.io.Serializable

class NavArgsToBundleWriter(
    private val codeGenerator: CodeOutputStreamMaker
) {

    private val importableHelper = ImportableHelper(navArgsToBundleTemplate.imports)

    fun write(
        generatedDestinations: List<GeneratedDestination>,
        generatedNavArgsList: List<DestinationGeneratingParamsWithNavArgs>
    ) {
        if (generatedDestinations.all { it.navArgsImportable == null }) {
            return
        }
        val file: OutputStream = codeGenerator.makeFile(
            packageName = codeGenBasePackageName,
            name = "NavArgsToBundles"
        )

        val destinationsWithNavArgs = generatedDestinations.filter { it.navArgsImportable != null }
            .associateBy { it.navArgsImportable!! }
            .values
            .toList()

        val navArgsList = generatedNavArgsList.filter { it.navArgs.isNotEmpty() }

        file.writeSourceFile(
            packageStatement = navArgsToBundleTemplate.packageStatement,
            importableHelper = importableHelper,
            sourceCode = navArgsToBundleTemplate.sourceCode
                .replace(
                    NAV_ARGS_TO_BUNDLE_WHEN_CASES,
                    navArgsToBundleMethodWhenCases(destinationsWithNavArgs, navArgsList)
                )
        )
    }

    private fun navArgsToBundleMethodWhenCases(
        destinations: List<GeneratedDestination>,
        navArgsList: List<DestinationGeneratingParamsWithNavArgs>
    ): String {
        val sb = StringBuilder()
        navArgsList.forEachIndexed { idx, it ->
            val destinationName = it.name
            val name =
                destinations.first { destinationName == it.simpleName }.navArgsImportable!!.qualifiedName
            val navArgs = it.navArgs
            val arguments = StringBuilder()

            sb += "fun ${name}.toBundle() = \n"
            sb += "Bundle().apply{"

            navArgs.forEach {
                if (!it.type.isNullable) {
                    arguments += when (it.type.importable.qualifiedName) {
                        Int::class.qualifiedName -> {
                            "\n\t\tputInt(\"${it.name}\", ${it.name})"
                        }
                        Float::class.qualifiedName -> {
                            "\n\t\tputFloat(\"${it.name}\", ${it.name})"
                        }
                        Long::class.qualifiedName -> {
                            "\n\t\tputLong(\"${it.name}\", ${it.name})"
                        }
                        Boolean::class.qualifiedName -> {
                            "\n\t\tputBoolean(\"${it.name}\", ${it.name})"
                        }
                        Byte::class.qualifiedName -> {
                            "\n\t\tputByte(\"${it.name}\", ${it.name})"
                        }
                        String::class.qualifiedName -> {
                            "\n\t\tputString(\"${it.name}\", ${it.name})"
                        }

                        IntArray::class.qualifiedName -> {
                            "\n\t\tputIntArray(\"${it.name}\", ${it.name})"
                        }

                        FloatArray::class.qualifiedName -> {
                            "\n\t\tputFloatArray(\"${it.name}\", ${it.name})"
                        }

                        LongArray::class.qualifiedName -> {
                            "\n\t\tputLongArray(\"${it.name}\", ${it.name})"
                        }
                        BooleanArray::class.qualifiedName -> {
                            "\n\t\tputBooleanArray(\"${it.name}\", ${it.name})"
                        }
                        ByteArray::class.qualifiedName -> {
                            "\n\t\tputByteArray(\"${it.name}\", ${it.name})"
                        }
                        Array::class.qualifiedName -> {
                            "\n\t\tputArray(\"${it.name}\", ${it.name})"
                        }
                        Serializable::class.qualifiedName -> {
                            "\n\t\tputSerializable(\"${it.name}\", ${it.name})"
                        }
                        ArrayList::class.qualifiedName -> {
                            val typeName =
                                (it.type.typeArguments.firstOrNull() as? TypeArgument.Typed)?.type?.importable?.qualifiedName
                                    ?: ""
                            when {
                                typeName == String::class.qualifiedName -> {
                                    "\n\t\tputStringArrayList(\"${it.name}\", ${it.name})"
                                }
                                typeName == Int::class.qualifiedName -> {
                                    "\n\t\tputIntegerArrayList(\"${it.name}\", ${it.name})"
                                }
                                typeName == CharSequence::class.qualifiedName -> {
                                    "\n\t\tputCharSequenceArrayList(\"${it.name}\", ${it.name})"
                                }
                                (it.type.typeArguments.firstOrNull() as? TypeArgument.Typed)?.type?.isParcelable == true -> {
                                    "\n\t\tputParcelableArrayList(\"${it.name}\", ${it.name})"
                                }
                                else -> ""
                            }
                        }
                        else -> ""
                    }

                    arguments += if (it.type.isParcelable) {
                        "\n\t\tputParcelable(\"${it.name}\", ${it.name})"
                    } else {
                        ""
                    }
                }
            }
            sb += "$arguments\n}"
            if (idx < navArgsList.lastIndex) {
                sb += "\n"
            }
        }

        return sb.toString()
    }
}
