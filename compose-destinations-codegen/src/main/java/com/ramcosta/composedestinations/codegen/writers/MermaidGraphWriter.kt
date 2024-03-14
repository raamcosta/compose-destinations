package com.ramcosta.composedestinations.codegen.writers

import com.ramcosta.composedestinations.codegen.DEFAULT_GEN_PACKAGE_NAME
import com.ramcosta.composedestinations.codegen.commons.RawNavGraphTree
import com.ramcosta.composedestinations.codegen.commons.plusAssign
import com.ramcosta.composedestinations.codegen.commons.snakeToCamelCase
import com.ramcosta.composedestinations.codegen.commons.toSnakeCase
import com.ramcosta.composedestinations.codegen.facades.CodeOutputStreamMaker
import com.ramcosta.composedestinations.codegen.model.CodeGenConfig
import com.ramcosta.composedestinations.codegen.model.CodeGenProcessedDestination
import com.ramcosta.composedestinations.codegen.model.ExternalRoute
import com.ramcosta.composedestinations.codegen.model.NavGraphGenParams
import java.io.File
import java.util.Locale

internal class MermaidGraphWriter(
    private val codeGenConfig: CodeGenConfig,
    private val codeGenerator: CodeOutputStreamMaker
) {

    fun write(graphTrees: List<RawNavGraphTree>) {
        graphTrees.forEach {
            writeMermaidGraph(it)
        }
    }

    private fun writeMermaidGraph(tree: RawNavGraphTree) {
        val title = tree.rawNavGraphGenParams.baseRoute
            .snakeToCamelCase()
            .replaceFirstChar { it.titlecase(Locale.ROOT) } + " Navigation Graph"
        val mermaidGraph = StringBuilder().apply {
            appendLine("---")
            appendLine("title: $title")
            appendLine("---")
            appendLine("%%{init: {'theme':'base', 'themeVariables': { 'primaryTextColor': '#fff' }}%%")
            appendLine("graph TD")
            appendGraphTreeLinks(tree)
            appendLine()
            append("@clicksPlaceholder@")
            appendLine()
            val destinationIds = tree.destinationIds()
            if (destinationIds.isNotEmpty()) {
                appendLine("classDef destination fill:#5383EC,stroke:#ffffff;")
                appendLine("class ${destinationIds.joinToString(",")} destination;")
            }
            val navGraphIds = tree.navGraphIds()
            if (navGraphIds.isNotEmpty()) {
                appendLine("classDef navgraph fill:#63BC76,stroke:#ffffff;")
                appendLine("class ${navGraphIds.joinToString(",")} navgraph;")
            }
        }.toString()

        if (codeGenConfig.mermaidGraph != null) {
            File(codeGenConfig.mermaidGraph, "${tree.rawNavGraphGenParams.name}.mmd")
                .writeText(
                    mermaidGraph
                        .replace("@clicksPlaceholder@", externalNavGraphClicks(tree, "mmd"))
                )
        } else {
            codeGenerator.makeFile(
                name = tree.rawNavGraphGenParams.name,
                packageName = "$DEFAULT_GEN_PACKAGE_NAME.mermaid",
                extensionName = "mmd",
            ).use {
                it += mermaidGraph
                    .replace("@clicksPlaceholder@", "")
            }
        }

        val htmlMermaid = html(title, mermaidGraph)
        if (codeGenConfig.htmlMermaidGraph != null) {
            File(codeGenConfig.htmlMermaidGraph, "${tree.rawNavGraphGenParams.name}.html")
                .writeText(
                    htmlMermaid.replace("@clicksPlaceholder@", externalNavGraphClicks(tree, "html"))
                )
        } else {
            codeGenerator.makeFile(
                name = tree.rawNavGraphGenParams.name,
                packageName = "$DEFAULT_GEN_PACKAGE_NAME.mermaid",
                extensionName = "html",
            ).use {
                it += htmlMermaid.replace("@clicksPlaceholder@", "")
            }
        }
    }

    private fun externalNavGraphClicks(
        tree: RawNavGraphTree,
        fileExtension: String
    ): String {
        val sb = StringBuilder()
        tree.findAllExternalNavGraphs().forEach { externalGraph ->
            sb.appendLine("click ${externalGraph.mermaidId} \"${externalGraph.generatedType.simpleName}.$fileExtension\" \"See ${externalGraph.mermaidVisualName} details\" _blank")
        }

        return sb.toString()
    }

    private fun StringBuilder.appendGraphTreeLinks(tree: RawNavGraphTree) {
        val graphNode = tree.node()
        val startRoute = requireNotNull(
            tree.destinations.find { it.isParentStart }
                ?: tree.nestedGraphs.find { it.isParentStart == true }
                ?: tree.externalStartRoute
        )

        appendLine(graphNode.link(startRoute.startRouteNode(), true))

        tree.destinations.removeStartRoute(startRoute).forEach { destination ->
            appendLine(graphNode.link(destination.node()))
        }

        tree.externalDestinations.removeStartRoute(startRoute).forEach { externalDestination ->
            appendLine(graphNode.link(externalDestination.node()))
        }

        tree.nestedGraphs.removeStartRoute(startRoute).forEach { nestedGraph ->
            appendLine(graphNode.link(nestedGraph.node()))
        }

        tree.externalNavGraphs.removeStartRoute(startRoute).forEach { externalNavGraph ->
            appendLine(graphNode.link(externalNavGraph.node()))
        }

        tree.nestedGraphs.forEach { nestedGraph ->
            appendGraphTreeLinks(nestedGraph)
        }
    }

    private fun <T> List<T>.removeStartRoute(startRoute: Any?) = filter { it != startRoute }

    private fun String.link(end: String, isStart: Boolean = false): String {
        val link = if (isStart) {
            """-- "start" ---"""
        } else {
            "---"
        }
        return "$this $link $end"
    }

    private fun RawNavGraphTree.destinationIds(): List<String> {
        return destinations.map { it.mermaidId } +
                externalDestinations.map { it.mermaidId } +
                nestedGraphs.flatMap { it.destinationIds() }
    }

    private fun RawNavGraphTree.navGraphIds(): List<String> {
        return (nestedGraphs + this).map { it.mermaidId } +
                externalNavGraphs.map { it.mermaidId } +
                nestedGraphs.flatMap { it.navGraphIds() }
    }

    private fun Any.startRouteNode(): String {
        return when (this) {
            is CodeGenProcessedDestination -> node()
            is NavGraphGenParams -> node()
            is ExternalRoute.Destination -> node()
            is ExternalRoute.NavGraph -> node()
            else -> error("oops?! that's not a node type ${this.javaClass.simpleName}")
        }
    }

    private fun NavGraphGenParams.node(): String {
        return """$mermaidId(["$mermaidVisualName"])"""
    }

    private fun CodeGenProcessedDestination.node(): String {
        return """$mermaidId("$mermaidVisualName")"""
    }

    private fun ExternalRoute.NavGraph.node(): String {
        return """$mermaidId(["$mermaidVisualName 🧩"])"""
    }

    private fun ExternalRoute.Destination.node(): String {
        return """$mermaidId("$mermaidVisualName 🧩")"""
    }

    private val ExternalRoute.NavGraph.mermaidId get() = generatedType.simpleName.toSnakeCase().replace("graph", "g")
    private val ExternalRoute.NavGraph.mermaidVisualName get() = generatedType.simpleName.run {
        if (endsWith("NavGraph")) {
            removeSuffix("NavGraph") + "Graph"
        } else if (endsWith("Graph")) {
            removeSuffix("Graph") + "NavGraph"
        } else {
            this
        }
    }

    private val ExternalRoute.Destination.mermaidId get() = generatedType.simpleName.toSnakeCase().replace("graph", "g")
    private val ExternalRoute.Destination.mermaidVisualName get() = generatedType.simpleName.removeSuffix("Destination")

    private val CodeGenProcessedDestination.mermaidId get() = baseRoute.replace("graph", "g")
    private val CodeGenProcessedDestination.mermaidVisualName get() = composableName

    private val NavGraphGenParams.mermaidId get() = baseRoute.replace("graph", "g")
    private val NavGraphGenParams.mermaidVisualName get() = annotationType.simpleName

    private fun RawNavGraphTree.findAllExternalNavGraphs(): List<ExternalRoute.NavGraph> {
        return externalNavGraphs + nestedGraphs.flatMap { it.externalNavGraphs }
    }

    private fun html(title: String, mermaidGraph: String) =
        """
        |<!DOCTYPE html>
        |<html lang="en">
        |<head>
        |    <meta charset="UTF-8">
        |    <title>$title</title>
        |</head>
        |<body>
        |<pre id='scene' class="mermaid">
        |$mermaidGraph
        |</pre>
        |<script src='https://unpkg.com/panzoom@9.4.0/dist/panzoom.min.js'></script>
        |<script type="module">import mermaid from 'https://cdn.jsdelivr.net/npm/mermaid@10/dist/mermaid.esm.min.mjs';</script>
        |<script>
        |var element = document.getElementById('scene')
        |var pz = panzoom(element, {
        |  minZoom: 0.5
        |});
        |
        |function reset(e) {
        |   pz.moveTo(0, 0); 
        |   pz.zoomAbs(0, 0, 1);
        |}
        |
        |function zoomIn(e) {
        |   pz.smoothZoom(0, 0, 1.25);
        |}
        |
        |function zoomOut(e) {
        |   pz.smoothZoom(0, 0, 0.75);
        |}
        |
        |document.addEventListener('keydown', function(event) {
        |    const keycode = event.keyCode;
        |    const key = event.key;
        |    
        |    switch(keycode) {
        |        case 82: // R key
        |            reset(event);
        |            break;
        |        case 187: // + key
        |            zoomIn(event);
        |            break;
        |        case 189: // - key
        |            zoomOut(event);
        |            break;
        |    }
        |});
        |</script>
        |</body>
        |</html>""".trimMargin()
}
