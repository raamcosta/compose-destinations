package com.ramcosta.composedestinations.codegen.writers

import com.ramcosta.composedestinations.codegen.codeGenDirectionNavGraph
import com.ramcosta.composedestinations.codegen.codeGenNavHostNavGraph
import com.ramcosta.composedestinations.codegen.codeGenTypedNavGraph
import com.ramcosta.composedestinations.codegen.commons.CORE_DIRECTION_NAV_GRAPH_SPEC
import com.ramcosta.composedestinations.codegen.commons.CORE_NAV_HOST_GRAPH_SPEC
import com.ramcosta.composedestinations.codegen.commons.CORE_TYPED_NAV_GRAPH_SPEC
import com.ramcosta.composedestinations.codegen.commons.GENERATED_NAV_GRAPH
import com.ramcosta.composedestinations.codegen.facades.CodeOutputStreamMaker
import com.ramcosta.composedestinations.codegen.model.CodeGenConfig
import com.ramcosta.composedestinations.codegen.model.CodeGenMode
import com.ramcosta.composedestinations.codegen.moduleName
import com.ramcosta.composedestinations.codegen.templates.core.setOfImportable
import com.ramcosta.composedestinations.codegen.templates.typeAliasDestination
import com.ramcosta.composedestinations.codegen.writers.helpers.ImportableHelper
import com.ramcosta.composedestinations.codegen.writers.helpers.writeSourceFile
import com.ramcosta.composedestinations.codegen.writers.sub.navGraphsPackageName

val typeAliasNavGraph = "${moduleName}$GENERATED_NAV_GRAPH"

const val MODULE_NAV_HOST_PLACEHOLDER = "@MODULE_NAV_HOST_PLACEHOLDER@"

internal class SealedNavGraphWriter(
    private val codeGenerator: CodeOutputStreamMaker,
    private val codeGenConfig: CodeGenConfig
) {

    fun write() {
        codeGenerator.makeFile(
            typeAliasNavGraph,
            navGraphsPackageName
        ).writeSourceFile(
            packageStatement = "package $navGraphsPackageName",
            importableHelper = ImportableHelper(
                setOfImportable(
                    CORE_DIRECTION_NAV_GRAPH_SPEC.qualifiedName,
                    CORE_NAV_HOST_GRAPH_SPEC.qualifiedName,
                    CORE_TYPED_NAV_GRAPH_SPEC.qualifiedName,
                    "$destinationsPackageName.$typeAliasDestination"
                )
            ),
            sourceCode = """
                
                public typealias $typeAliasNavGraph = $codeGenTypedNavGraph<*, *>

                public sealed class $codeGenTypedNavGraph<NAV_ARGS, START_ROUTE_NAV_ARGS>: ${CORE_TYPED_NAV_GRAPH_SPEC.simpleName}<NAV_ARGS, START_ROUTE_NAV_ARGS> {
                    public abstract val destinations: List<$typeAliasDestination>

                    public final override val destinationsByRoute: Map<String, $typeAliasDestination> = destinations.associateBy { it.route }
                }

                public sealed class $codeGenDirectionNavGraph: $codeGenTypedNavGraph<Unit, Unit>(), ${CORE_DIRECTION_NAV_GRAPH_SPEC.simpleName}

                $MODULE_NAV_HOST_PLACEHOLDER
                
            """.trimIndent()
                .replace(
                    MODULE_NAV_HOST_PLACEHOLDER,
                    if (codeGenConfig.mode is CodeGenMode.SingleModule) {
                        "public sealed class $codeGenNavHostNavGraph: $codeGenDirectionNavGraph(), ${CORE_NAV_HOST_GRAPH_SPEC.simpleName}"
                    } else {
                        ""
                    }
                )
        )
    }
}