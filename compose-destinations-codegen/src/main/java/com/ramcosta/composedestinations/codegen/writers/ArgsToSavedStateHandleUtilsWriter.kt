package com.ramcosta.composedestinations.codegen.writers

import com.ramcosta.composedestinations.codegen.codeGenBasePackageName
import com.ramcosta.composedestinations.codegen.commons.RawNavGraphTree
import com.ramcosta.composedestinations.codegen.commons.sourceIds
import com.ramcosta.composedestinations.codegen.facades.CodeOutputStreamMaker
import com.ramcosta.composedestinations.codegen.model.CodeGenProcessedDestination
import com.ramcosta.composedestinations.codegen.model.CustomNavType
import com.ramcosta.composedestinations.codegen.model.RawNavArgsClass
import com.ramcosta.composedestinations.codegen.model.SubModuleInfo
import com.ramcosta.composedestinations.codegen.model.Type
import com.ramcosta.composedestinations.codegen.templates.ALL_ARGS_SAVED_STATE_HANDLE_FUNCTIONS_PLACEHOLDER
import com.ramcosta.composedestinations.codegen.templates.ARGS_DATA_CLASS_SIMPLE_NAME
import com.ramcosta.composedestinations.codegen.templates.NAV_TYPE_PUT_IN_BUNDLE_CALLS_PLACEHOLDER
import com.ramcosta.composedestinations.codegen.templates.VISIBILITY_PLACEHOLDER
import com.ramcosta.composedestinations.codegen.templates.argsToSavedStateHandleTemplate
import com.ramcosta.composedestinations.codegen.templates.core.setOfImportable
import com.ramcosta.composedestinations.codegen.writers.helpers.ImportableHelper
import com.ramcosta.composedestinations.codegen.writers.helpers.NavArgResolver
import com.ramcosta.composedestinations.codegen.writers.helpers.writeSourceFile

internal class ArgsToSavedStateHandleUtilsWriter(
    private val codeGenerator: CodeOutputStreamMaker,
    private val submodules: List<SubModuleInfo>,
    customNavTypeByType: Map<Type, CustomNavType>,
) {

    private val importableHelper = ImportableHelper(
        argsToSavedStateHandleTemplate.imports + if (customNavTypeByType.isEmpty()) {
            emptySet()
        } else {
            setOfImportable("$codeGenBasePackageName.navtype.*")
        }
    )
    private val navArgsResolver = NavArgResolver(
        customNavTypeByType,
        importableHelper
    )

    private val argsToSavedStateHandleFunctionTemplate = """
$VISIBILITY_PLACEHOLDER fun $ARGS_DATA_CLASS_SIMPLE_NAME.toSavedStateHandle(
    handle: SavedStateHandle = SavedStateHandle()
): SavedStateHandle {$NAV_TYPE_PUT_IN_BUNDLE_CALLS_PLACEHOLDER
    return handle
}
""".trimIndent()

    fun write(
        generatedDestinations: List<CodeGenProcessedDestination>,
        navGraphTrees: List<RawNavGraphTree>
    ) {
        val allNavArgsClasses: Set<RawNavArgsClass> =
            generatedDestinations.mapNotNullTo(mutableSetOf()) { it.navArgsClass } +
                    navGraphTrees.mapNotNullTo(mutableSetOf()) {
                        if (it.requiresGeneratingNavArgsClass()) {
                            return@mapNotNullTo RawNavArgsClass(
                                parameters = it.navArgs!!.parameters,
                                visibility = it.visibility,
                                type = it.genNavArgsType,
                                extraStartRouteArgs = it.startRouteArgs
                            )
                        }

                        if (it.hasOnlyGraphArgs()) {
                            return@mapNotNullTo it.navArgs
                        }

                        null
                    }

        if (allNavArgsClasses.isEmpty()) {
            return
        }

        // add nav args packages as imports so we can delegate start route args to submodules generated code
        setOfImportable(
            *submodules.mapNotNull { subModule ->
                "${subModule.genPackageName}.navargs.*".takeIf { subModule.hasNavArgsPackage }
            }.toTypedArray()
        ).forEach {
            importableHelper.addPriorityQualifiedImport(it)
        }

        codeGenerator.makeFile(
            packageName = "$codeGenBasePackageName.navargs",
            name = "ArgsToSavedStateHandle",
            sourceIds = sourceIds(generatedDestinations, navGraphTrees).toTypedArray()
        ).writeSourceFile(
            packageStatement = argsToSavedStateHandleTemplate.packageStatement,
            importableHelper = importableHelper,
            sourceCode = argsToSavedStateHandleTemplate.sourceCode
                .replace(ALL_ARGS_SAVED_STATE_HANDLE_FUNCTIONS_PLACEHOLDER, argsToSavedStateHandleCode(allNavArgsClasses))
        )
    }

    private fun argsToSavedStateHandleCode(allNavArgsClasses: Set<RawNavArgsClass>): String {
        return allNavArgsClasses.map { argsClass ->
            val startRouteArgsCode = if (argsClass.extraStartRouteArgs != null) {
                "\n\tstartRouteArgs.toSavedStateHandle(handle)"
            } else {
                ""
            }

            argsToSavedStateHandleFunctionTemplate
                .replace(VISIBILITY_PLACEHOLDER, argsClass.visibility.name.lowercase())
                .replace(ARGS_DATA_CLASS_SIMPLE_NAME, importableHelper.addAndGetPlaceholder(argsClass.type))
                .replace(
                    NAV_TYPE_PUT_IN_BUNDLE_CALLS_PLACEHOLDER,
                    argsClass.parameters.map { param ->
                        navArgsResolver.resolveToSavedStateHandle(param)
                    }.joinToString("") { "\n\t$it" }
                    + startRouteArgsCode
                )
        }.joinToString("") { "\n\n$it" }
            .removePrefix("\n\n")
    }
}
