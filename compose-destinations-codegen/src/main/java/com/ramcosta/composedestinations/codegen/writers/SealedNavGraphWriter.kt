package com.ramcosta.composedestinations.codegen.writers

import com.ramcosta.composedestinations.codegen.commons.*
import com.ramcosta.composedestinations.codegen.facades.CodeOutputStreamMaker
import com.ramcosta.composedestinations.codegen.templates.core.setOfImportable
import com.ramcosta.composedestinations.codegen.templates.typeAliasDestination
import com.ramcosta.composedestinations.codegen.writers.helpers.ImportableHelper
import com.ramcosta.composedestinations.codegen.writers.helpers.writeSourceFile
import com.ramcosta.composedestinations.codegen.writers.sub.navGraphsPackageName

internal class SealedNavGraphWriter(
    private val codeGenerator: CodeOutputStreamMaker
) {

    fun write() {
        codeGenerator.makeFile(
            "NavGraph",
            navGraphsPackageName
        ).writeSourceFile(
            packageStatement = "package $navGraphsPackageName",
            importableHelper = ImportableHelper(
                setOfImportable(
                    "$CORE_PACKAGE_NAME.spec.DirectionNavGraphSpec",
                    "$CORE_PACKAGE_NAME.spec.TypedNavGraphSpec",
                    "$destinationsPackageName.$typeAliasDestination"
                )
            ),
            sourceCode = """
                
                typealias $GENERATED_NAV_GRAPH = $GENERATED_TYPED_NAV_GRAPH<*>

                sealed class $GENERATED_TYPED_NAV_GRAPH<T>: ${CORE_TYPED_NAV_GRAPH_SPEC.simpleName}<T> {
                    abstract val destinations: List<$typeAliasDestination>

                    final override val destinationsByRoute = destinations.associateBy { it.route }
                }

                sealed class $GENERATED_DIRECTION_NAV_GRAPH: $GENERATED_TYPED_NAV_GRAPH<Unit>(), ${CORE_DIRECTION_NAV_GRAPH_SPEC.simpleName}
                
            """.trimIndent()
        )
    }
}