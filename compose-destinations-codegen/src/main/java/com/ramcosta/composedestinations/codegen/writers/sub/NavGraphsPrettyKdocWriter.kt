package com.ramcosta.composedestinations.codegen.writers.sub

import com.ramcosta.composedestinations.codegen.commons.RawNavGraphTree
import com.ramcosta.composedestinations.codegen.model.CodeGenProcessedDestination
import com.ramcosta.composedestinations.codegen.model.Importable
import com.ramcosta.composedestinations.codegen.writers.helpers.ImportableHelper

internal class NavGraphsPrettyKdocWriter(
    private val importableHelper: ImportableHelper,
    private val topLevelGraphs: List<RawNavGraphTree>
) {

    private val minIndent = "        " // 8 spaces

    fun write(
    ): String {
        val sb = StringBuilder()
            .append("\n *\n")
            .append("""
                 | * -------------------------------------------------------
                 | * | Legend:                                             |
                 | * | - 🗺️: Navigation graph                              |
                 | * | - 📍: Destination                                   |
                 | * | - 🏁: Marks 🗺️/📍as the start of the parent graph   |
                 | * -------------------------------------------------------
            """.trimMargin())
            .append("\n *\n")

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
        append("[${importableHelper.addAndGetPlaceholder(navGraphTree.type)}]")
        appendNewLines()

        navGraphTree.destinations
            .sortedBy { if (it.navGraphInfo.start) 0 else 1 }
            .forEachIndexed { idx, it ->
                appendDestination(it, depth + 1)

                if (idx < navGraphTree.destinations.lastIndex) {
                    appendNewLines()
                }
            }

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

    private fun StringBuilder.appendDestination(
        destination: CodeGenProcessedDestination,
        depth: Int,
    ) {
        append(" * ∙")
        appendDepthRelatedPadding(depth)
        appendDestinationIcon(true)
        if (destination.navGraphInfo.start) {
            appendStartIcon()
        }
        append("[${importableHelper.addAndGetPlaceholder(Importable(destination.composableName, destination.composableQualifiedName))}]")
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
}
