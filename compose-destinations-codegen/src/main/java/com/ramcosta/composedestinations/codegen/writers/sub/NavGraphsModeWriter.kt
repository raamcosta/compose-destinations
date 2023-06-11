package com.ramcosta.composedestinations.codegen.writers.sub

import com.ramcosta.composedestinations.codegen.commons.IllegalDestinationsSetup
import com.ramcosta.composedestinations.codegen.commons.RawNavGraphTree
import com.ramcosta.composedestinations.codegen.commons.rootNavGraphGenParams
import com.ramcosta.composedestinations.codegen.facades.CodeOutputStreamMaker
import com.ramcosta.composedestinations.codegen.model.CodeGenConfig
import com.ramcosta.composedestinations.codegen.model.CustomNavType
import com.ramcosta.composedestinations.codegen.model.Type
import com.ramcosta.composedestinations.codegen.templates.moduleNavGraphTemplate
import com.ramcosta.composedestinations.codegen.writers.SealedNavGraphWriter
import com.ramcosta.composedestinations.codegen.writers.helpers.ImportableHelper
import com.ramcosta.composedestinations.codegen.writers.helpers.NavArgResolver

internal class NavGraphsModeWriter(
    private val codeGenerator: CodeOutputStreamMaker,
    private val codeGenConfig: CodeGenConfig,
    private val sealedNavGraphWriter: SealedNavGraphWriter,
    private val customNavTypeByType: Map<Type, CustomNavType>,
    private val singleNavGraphWriter: (
        CodeOutputStreamMaker,
        ImportableHelper,
        RawNavGraphTree,
        NavArgResolver
    ) -> SingleNavGraphWriter,
) {

    private fun navGraphWriter(navGraph: RawNavGraphTree): SingleNavGraphWriter {
        val importableHelper = ImportableHelper(moduleNavGraphTemplate.imports)
        return singleNavGraphWriter(
            codeGenerator,
            importableHelper,
            navGraph,
            NavArgResolver(customNavTypeByType, importableHelper)
        )
    }

    fun write(
        graphTrees: List<RawNavGraphTree>
    ) {
        val navGraphTrees = graphTrees.toMutableList()

        val indexOfRootNavGraph =
            navGraphTrees.indexOfFirst { it.rawNavGraphGenParams == rootNavGraphGenParams }
        if (indexOfRootNavGraph != -1) {
            val moduleName = codeGenConfig.moduleName ?: throw IllegalDestinationsSetup(
                "You need to set 'moduleName' on gradle ksp configuration to be used as" +
                        " main nav graph name or use a NavGraph annotation on all Destinations of this module."
            )

            val newRootWithModuleName =
                rootNavGraphGenParams
                    .copy(isNavHostGraph = false)
                    .copyWithNameForRoute(newRoute = moduleName)

            val isThereAlreadyOneWithTheNameOrRoute =
                navGraphTrees.any { it.anyGraphCollidesWith(newRootWithModuleName) }

            if (isThereAlreadyOneWithTheNameOrRoute) {
                throw IllegalDestinationsSetup(
                    "Code gen mode was set to 'navgraphs' but you're using both `RootNavGraph` " +
                            "and a NavGraph with the module name. On this mode, `RootNavGraph` is replaced " +
                            "by one with the name's module, so these collide!\n" +
                            "FIX: Create your own NavGraph annotation with `NavGraph(default = true)` to be used instead of `RootNavGraph`"
                )
            }

            val treeToChange = navGraphTrees[indexOfRootNavGraph]
            navGraphTrees[indexOfRootNavGraph] = treeToChange.copy(
                rawNavGraphGenParams = newRootWithModuleName
            )
        }

        navGraphTrees.forEach {
            writeNavGraphTreeRecursively(it)
        }
        sealedNavGraphWriter.write()
    }

    private fun writeNavGraphTreeRecursively(
        graphTree: RawNavGraphTree
    ) {
        graphTree.nestedGraphs.forEach {
            writeNavGraphTreeRecursively(it)
        }
        navGraphWriter(graphTree).write()
    }
}
