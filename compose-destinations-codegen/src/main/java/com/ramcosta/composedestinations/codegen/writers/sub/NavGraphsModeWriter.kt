package com.ramcosta.composedestinations.codegen.writers.sub

import com.ramcosta.composedestinations.codegen.commons.*
import com.ramcosta.composedestinations.codegen.facades.CodeOutputStreamMaker
import com.ramcosta.composedestinations.codegen.model.*
import com.ramcosta.composedestinations.codegen.templates.*
import com.ramcosta.composedestinations.codegen.writers.SealedNavGraphWriter
import com.ramcosta.composedestinations.codegen.writers.helpers.ImportableHelper

internal class NavGraphsModeWriter(
    private val codeGenerator: CodeOutputStreamMaker,
    private val codeGenConfig: CodeGenConfig,
    private val sealedNavGraphWriter: SealedNavGraphWriter,
    private val singleNavGraphWriter: (
        CodeOutputStreamMaker,
        SingleNavGraphWriter.Config,
        ImportableHelper
    ) -> SingleNavGraphWriter,
) {
    private val importableHelper = ImportableHelper(moduleNavGraphTemplate.imports)

    private val navGraphWriter = singleNavGraphWriter(
        codeGenerator,
        SingleNavGraphWriter.Config(
            navGraphType = CORE_TYPED_NAV_GRAPH_SPEC,
            navTypeInterface = true,
            directionNavGraphType = CORE_DIRECTION_NAV_GRAPH_SPEC,
            directionTypeInterface = true,
            destinationsCollectionIsMap = true,
            destinationsCollectionName = "destinationsByRoute"
        ),
        importableHelper
    )

    fun write(
        graphTrees: List<RawNavGraphTree>
    ) {
        val navGraphTrees = graphTrees.toMutableList()

        val indexOfRootNavGraph =
            navGraphTrees.indexOfFirst { it.node.rawNavGraphGenParams == rootNavGraphGenParams }
        if (indexOfRootNavGraph != -1) {
            val moduleName = codeGenConfig.moduleName ?: throw IllegalDestinationsSetup(
                "You need to set 'moduleName' on gradle ksp configuration to be used as" +
                        " main nav graph name or use a NavGraph annotation on all Destinations of this module."
            )

            val newRootWithModuleName =
                rootNavGraphGenParams.copyWithNameForRoute(newRoute = moduleName)

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
                node = treeToChange.node.copy(rawNavGraphGenParams = newRootWithModuleName)
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
        navGraphWriter.write(graphTree)
    }
}
