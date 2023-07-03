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
            sb.appendGraphPrettyKdoc(it, 0)//, minIndent.length)

            if (idx < topLevelGraphs.lastIndex) {
                sb.append("\n *\n")
            }
        }

        return sb.toString()
    }

    private fun StringBuilder.appendGraphPrettyKdoc(
        navGraphTree: RawNavGraphTree,
        depth: Int,
//        indentationBeforeGenIcon: Int,
    ) {
        val isNested = depth > 0
//        var biggestChildElementSize = (
//                    navGraphTree.nestedGraphs.map { it.type.preferredSimpleName.length + if (it.isParentStart == true) 2 else 0 } +
//                    navGraphTree.destinations.map { it.composableName.length + if(it.navGraphInfo.start) 2 else 0 }
//                ).max()
//        val currentGenIconColumn = depth * 8 + navGraphTree.type.preferredSimpleName.length + 2 + 2 + indentationBeforeGenIcon +
//                if (isNested) 1 else 0 + if (navGraphTree.isParentStart == true) 2 else 0
//        val minChildGenIconColumn = currentGenIconColumn + minIndent.length
//
//        val biggestChildElementGenIconColumn = (depth + 1) * 8 + biggestChildElementSize + 2 + 2 + 1 + minIndent.length
//
//        val additionToBiggestSize = if (biggestChildElementGenIconColumn >= minChildGenIconColumn) {
//            0
//        } else {
//            minChildGenIconColumn - biggestChildElementGenIconColumn
//        }
//        biggestChildElementSize += additionToBiggestSize

        append(" * ∙")
        appendDepthRelatedPadding(depth)
        appendGraphIcon(isNested)
        if (navGraphTree.isParentStart == true) {
            appendStartIcon()
        }
        append("[${importableHelper.addAndGetPlaceholder(navGraphTree.type)}]")
//        append(CharArray(indentationBeforeGenIcon) { ' ' })
//        appendGeneratedElementIcon()
//        append(" ")

//        append(CharArray(indentationBeforeGenIcon) { ' ' })
//        append(" → ")
//        append("[${importableHelper.addAndGetPlaceholder(navGraphTree.navGraphImportable)}]")
        appendNewLines()

        navGraphTree.destinations
            .sortedBy { if (it.navGraphInfo.start) 0 else 1 }
            .forEachIndexed { idx, it ->
//                val indentation =
//                    biggestChildElementSize - it.composableName.length + minIndent.length - if (it.navGraphInfo.start) 2 else 0

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
//                val indentation =
//                    biggestChildElementSize - it.type.preferredSimpleName.length + minIndent.length - if(it.isParentStart == true) 2 else 0

                appendGraphPrettyKdoc(it, depth + 1)//, indentation)

                if (idx < navGraphTree.nestedGraphs.lastIndex) {
                    appendNewLines()
                }
            }
    }

    private fun StringBuilder.appendDestination(
        destination: CodeGenProcessedDestination,
        depth: Int,
//        indentation: Int
    ) {
        append(" * ∙")
        appendDepthRelatedPadding(depth)
        appendDestinationIcon(true)
        if (destination.navGraphInfo.start) {
            appendStartIcon()
        }
        append("[${importableHelper.addAndGetPlaceholder(Importable(destination.composableName, destination.composableQualifiedName))}]")
//        append(CharArray(indentation) { ' ' })
//        appendGeneratedElementIcon()
//        append(" ")

//        append(CharArray(indentation) { ' ' })
//        append(" → ")
//        append("[${importableHelper.addAndGetPlaceholder(destination.destinationImportable)}]")
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

//    private fun StringBuilder.appendGeneratedElementIcon() {
//        append("""⚙️""")
//    }

    private fun StringBuilder.appendStartIcon(): StringBuilder {
        append("""🏁""")
        return this
    }
}
