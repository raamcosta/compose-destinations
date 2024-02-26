package com.ramcosta.composedestinations.codegen.writers

import com.ramcosta.composedestinations.codegen.codeGenBasePackageName
import com.ramcosta.composedestinations.codegen.commons.RawNavGraphTree
import com.ramcosta.composedestinations.codegen.commons.plusAssign
import com.ramcosta.composedestinations.codegen.commons.snakeToCamelCase
import com.ramcosta.composedestinations.codegen.commons.toSnakeCase
import com.ramcosta.composedestinations.codegen.facades.CodeOutputStreamMaker
import com.ramcosta.composedestinations.codegen.model.CodeGenProcessedDestination
import com.ramcosta.composedestinations.codegen.model.ExternalRoute
import com.ramcosta.composedestinations.codegen.model.NavGraphGenParams
import java.util.Locale

internal class MermaidGraphWriter(
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
            appendLine("classDef destination fill:#5383EC,stroke:#ffffff;")
            appendLine("class ${tree.destinationIds().joinToString(",")} destination;")
            appendLine("classDef navgraph fill:#63BC76,stroke:#ffffff;")
            appendLine("class ${tree.navGraphIds().joinToString(",")} navgraph;")
        }.toString()

        codeGenerator.makeFile(
            name = tree.rawNavGraphGenParams.name,
            packageName = "$codeGenBasePackageName.mermaid",
            extensionName = "mmd",
        ).use {
            it += mermaidGraph
        }
        codeGenerator.makeFile(
            name = tree.rawNavGraphGenParams.name,
            packageName = "$codeGenBasePackageName.mermaid",
            extensionName = "html",
        ).use {
            it += html(title, mermaidGraph)
        }
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
        return destinations.map { it.baseRoute } +
                externalDestinations.map { it.generatedType.simpleName.toSnakeCase() } +
                nestedGraphs.flatMap { it.destinationIds() }
    }

    private fun RawNavGraphTree.navGraphIds(): List<String> {
        return (nestedGraphs + this).map { it.baseRoute } +
                externalNavGraphs.map { it.generatedType.simpleName.toSnakeCase() } +
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
        val id = baseRoute
        val visualName = annotationType.simpleName

        return """$id(["$visualName"])"""
    }

    private fun CodeGenProcessedDestination.node(): String {
        val id = baseRoute
        val visualName = composableName

        return """$id("$visualName")"""
    }

    private fun ExternalRoute.NavGraph.node(): String {
        val id = generatedType.simpleName.toSnakeCase()
        val visualName = generatedType.simpleName.run {
            if (endsWith("NavGraph")) {
                removeSuffix("NavGraph") + "Graph"
            } else if (endsWith("Graph")) {
                removeSuffix("Graph") + "NavGraph"
            } else {
                this
            }
        }

        return """$id(["$visualName 🧩"])"""
    }

    private fun ExternalRoute.Destination.node(): String {
        val id = generatedType.simpleName.toSnakeCase()
        val visualName = generatedType.simpleName.removeSuffix("Destination")

        return """$id("$visualName 🧩")"""
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
        |  minZoom: 0.1
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
