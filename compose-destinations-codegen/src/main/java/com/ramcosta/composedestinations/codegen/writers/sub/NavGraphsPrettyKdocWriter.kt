package com.ramcosta.composedestinations.codegen.writers.sub

import com.ramcosta.composedestinations.codegen.commons.RawNavGraphTree
import com.ramcosta.composedestinations.codegen.model.Importable
import com.ramcosta.composedestinations.codegen.writers.helpers.ImportableHelper

internal class NavGraphsPrettyKdocWriter(
    private val importableHelper: ImportableHelper,
    private val topLevelGraphs: List<RawNavGraphTree>
) {

    private val minIndent = "        " // 8 spaces

    fun write(includeLegend: Boolean = true): String {
        val sb = StringBuilder()
            .append("\n *\n")

        if (includeLegend) {
            sb.append(
                """
                 | * -------------------------------------------------------
                 | * | Legend:                                             |
                 | * | - 🗺️: Navigation graph                              |
                 | * | - 📍: Destination                                   |
                 | * | - 🏁: Marks 🗺️/📍as the start of the parent graph   |
                 | * | - 🧩: 🗺️/📍is generated on external module          |
                 | * -------------------------------------------------------
            """.trimMargin()
            ).append("\n *\n")
        }

        topLevelGraphs.forEachIndexed { idx, it ->
            sb.appendGraphPrettyKdoc(it, 0)

            if (idx < topLevelGraphs.lastIndex) {
                sb.append("\n *\n")
            }
        }

        return sb.toString()
    }

    private fun StringBuilder.appendGraphPrettyKdoc(
        navGraphTree: RawNavGraphTree,
        depth: Int,
    ) {
        val isNested = depth > 0
        append(" * ∙")
        appendDepthRelatedPadding(depth)
        appendGraphIcon(isNested)
        if (navGraphTree.isParentStart == true) {
            appendStartIcon()
        }
        append("[${importableHelper.addAndGetPlaceholder(navGraphTree.annotationType)}]")

        addDestinations(navGraphTree, depth)

        addNestedNavGraphs(navGraphTree, depth)

        addExternalNavGraphs(navGraphTree, depth)
    }

    private fun StringBuilder.addDestinations(
        navGraphTree: RawNavGraphTree,
        depth: Int
    ) {
        val allDestinations =
            navGraphTree.destinations.map {
                KdocRoute(
                    false,
                    it.navGraphInfo.start,
                    Importable(it.composableName, it.composableQualifiedName),
                    true
                )
            } +
                    navGraphTree.externalDestinations.map {
                        KdocRoute(
                            true,
                            it.generatedType == navGraphTree.externalStartRoute?.generatedType,
                            it.generatedType,
                            true
                        )
                    }

        if (allDestinations.isNotEmpty()) {
            appendNewLines()
        }

        allDestinations
            .sortedBy { if (it.isStart) 0 else 1 }
            .forEachIndexed { idx, it ->
                appendKdocRoute(it, depth + 1)

                if (idx < allDestinations.lastIndex) {
                    appendNewLines()
                }
            }
    }

    private fun StringBuilder.addNestedNavGraphs(
        navGraphTree: RawNavGraphTree,
        depth: Int
    ) {
        if (navGraphTree.nestedGraphs.isNotEmpty()) {
            appendNewLines()
        }

        navGraphTree.nestedGraphs
            .sortedBy { if (it.isParentStart == true) 0 else 1 }
            .forEachIndexed { idx, it ->
                appendGraphPrettyKdoc(it, depth + 1)

                if (idx < navGraphTree.nestedGraphs.lastIndex) {
                    appendNewLines()
                }
            }
    }

    private fun StringBuilder.addExternalNavGraphs(
        navGraphTree: RawNavGraphTree,
        depth: Int
    ) {
        if (navGraphTree.externalNavGraphs.isNotEmpty()) {
            appendNewLines()
        }

        navGraphTree.externalNavGraphs
            .sortedBy { if (it == navGraphTree.externalStartRoute) 0 else 1 }
            .forEachIndexed { idx, it ->
                appendKdocRoute(
                    KdocRoute(
                        true,
                        it == navGraphTree.externalStartRoute,
                        it.generatedType,
                        false
                    ),
                    depth + 1
                )

                if (idx < navGraphTree.externalNavGraphs.lastIndex) {
                    appendNewLines()
                }
            }
    }

    private fun StringBuilder.appendKdocRoute(
        kdocRoute: KdocRoute,
        depth: Int,
    ) {
        append(" * ∙")
        appendDepthRelatedPadding(depth)
        if (kdocRoute.isDestination) {
            appendDestinationIcon(true)
        } else {
            appendGraphIcon(true)
        }
        if (kdocRoute.isStart) {
            appendStartIcon()
        }
        append("[${importableHelper.addAndGetPlaceholder(kdocRoute.type)}]")
        if (kdocRoute.isExternal) {
            appendExternalIcon()
        }
        if (!kdocRoute.isDestination) {
            append("\n")
            append(" * ∙")
            appendDepthRelatedPadding(depth + 1)
            append("[...]")
        }
    }

    private fun StringBuilder.appendDepthRelatedPadding(depth: Int) {
        repeat(depth) {
            append(minIndent)
        }
    }

    private fun StringBuilder.appendNewLines() {
        append("\n")
    }

    private fun StringBuilder.appendDestinationIcon(isNested: Boolean = false): StringBuilder {
        if (isNested) {
            append("↳")
        }
        append("""📍""")
        return this
    }

    private fun StringBuilder.appendGraphIcon(isNested: Boolean = false): StringBuilder {
        if (isNested) {
            append("↳")
        }
        append("""🗺️""")

        return this
    }

    private fun StringBuilder.appendStartIcon(): StringBuilder {
        append("""🏁""")
        return this
    }

    private fun StringBuilder.appendExternalIcon(): StringBuilder {
        append("""🧩""")
        return this
    }

    private class KdocRoute(
        val isExternal: Boolean,
        val isStart: Boolean,
        val type: Importable,
        val isDestination: Boolean,
    )
}
