package com.ramcosta.composedestinations.ksp

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.SourceFile.Companion.kotlin
import com.tschuchort.compiletesting.kspIncremental
import com.tschuchort.compiletesting.kspSourcesDir
import com.tschuchort.compiletesting.symbolProcessorProviders
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@Ignore("TODO until we find out what's the issue with jvm targets on tests")
class ProcessorProviderTests {
    @Rule
    @JvmField
    var temporaryFolder: TemporaryFolder = TemporaryFolder()

    @Test
    fun `@Destination basic setup`() {
        val compilation = prepareCompilation(
            kotlin(
                "Screen.kt",
                """
          package test

          import com.ramcosta.composedestinations.annotation.Destination
          import com.ramcosta.composedestinations.annotation.RootNavGraph

          @RootNavGraph(start = true)
          @Destination(route = "test1")
          fun TestScreen1() {}

          @Destination(route = "test2")
          fun TestScreen2() {}
          """.trimIndent()
            )
        )
        val result = compilation.compile()

        assertEquals(result.exitCode, KotlinCompilation.ExitCode.OK)
        assertTrue(
            compilation.kspSourcesDir.walkTopDown()
                .filter { it.nameWithoutExtension == "TestScreen1Destination" || it.nameWithoutExtension == "TestScreen2Destination" }
                .toList().isNotEmpty()
        )
    }

    @Test
    fun `@Destination should have at least one start`() {
        val result = compile(
            kotlin(
                "Screen.kt",
                """
          package test

          import com.ramcosta.composedestinations.annotation.Destination

          @Destination(route = "test1")
          fun TestScreen1() {}

          @Destination(route = "test2")
          fun TestScreen2() {}
          """.trimIndent()
            )
        )

        assertEquals(result.exitCode, KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertTrue(result.messages.contains("com.ramcosta.composedestinations.codegen.commons.IllegalDestinationsSetup: Use argument `start = true` in the @Destination annotation of the 'root' nav graph's start destination!"))
    }

    @Test
    fun `@Destination with navArgsDelegate no serializable`() {
        val result = compile(
            kotlin(
                "Screen.kt",
                """
          package com.ramcosta.composedestinations.example

          import com.ramcosta.composedestinations.annotation.Destination
          import com.ramcosta.composedestinations.annotation.RootNavGraph

          data class NotSerializable(
            val color: String
          )

          data class TestArgs(
            val arg: NotSerializable
          )

          @RootNavGraph(start = true)
          @Destination(route = "test1")
          fun TestScreen1() {}

          @Destination(route = "test2", navArgsDelegate = TestArgs::class)
          fun TestScreen2(
            navArgs: TestArgs
          ) {}
          """.trimIndent()
            )
        )

        assertEquals(result.exitCode, KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertTrue(result.messages.contains("com.ramcosta.composedestinations.codegen.commons.IllegalDestinationsSetup: Composable 'TestScreen2': 'navArgsDelegate' cannot have arguments that are not navigation types."))
    }

    @Test
    fun `@Destination with navArgsDelegate serializable`() {
        val compilation = prepareCompilation(
            kotlin(
                "Screen.kt",
                """
          package com.ramcosta.composedestinations.example

          import com.ramcosta.composedestinations.annotation.Destination
          import kotlinx.serialization.Serializable
          import com.ramcosta.composedestinations.annotation.RootNavGraph

          @Serializable
          data class IsSerializable(
            val color: String
          )

          data class TestArgs(
            val arg: IsSerializable
          )

          @RootNavGraph(start = true)
          @Destination(route = "test1")
          fun TestScreen1() {}

          @Destination(route = "test2", navArgsDelegate = TestArgs::class)
          fun TestScreen2(
            navArgs: TestArgs
          ) {}
          """.trimIndent()
            )
        )

        val result = compilation.compile()

        assertEquals(result.exitCode, KotlinCompilation.ExitCode.OK)
        assertTrue(
            compilation.kspSourcesDir.walkTopDown()
                .filter { it.nameWithoutExtension == "TestScreen1Destination" || it.nameWithoutExtension == "TestScreen2Destination" }
                .toList().isNotEmpty()
        )
    }

    @Test
    fun `@Destination with navArgsDelegate non serializable typealias`() {
        val result = compile(
            kotlin(
                "Screen.kt",
                """
          package com.ramcosta.composedestinations.example

          import com.ramcosta.composedestinations.annotation.Destination
          import com.ramcosta.composedestinations.annotation.RootNavGraph

          data class NotSerializable(
            val color: String
          )

          typealias Aliased = NotSerializable

          data class TestArgs(
            val arg: Aliased
          )

          @RootNavGraph(start = true)
          @Destination(route = "test1")
          fun TestScreen1() {}

          @Destination(route = "test2", navArgsDelegate = TestArgs::class)
          fun TestScreen2(
            navArgs: TestArgs
          ) {}
          """.trimIndent()
            )
        )

        assertEquals(result.exitCode, KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertTrue(result.messages.contains("com.ramcosta.composedestinations.codegen.commons.IllegalDestinationsSetup: Composable 'TestScreen2': 'navArgsDelegate' cannot have arguments that are not navigation types."))
    }

    @Test
    fun `@Destination with navArgsDelegate serializable typealias`() {
        val compilation = prepareCompilation(
            kotlin(
                "Screen.kt",
                """
          package com.ramcosta.composedestinations.example

          import com.ramcosta.composedestinations.annotation.Destination
          import com.ramcosta.composedestinations.annotation.RootNavGraph
          import kotlinx.serialization.Serializable

          @Serializable
          data class IsSerializable(
            val color: String
          )

          typealias Aliased = IsSerializable

          data class TestArgs(
            val arg: Aliased
          )

          @RootNavGraph(start = true)
          @Destination(route = "test1")
          fun TestScreen1() {}

          @Destination(route = "test2", navArgsDelegate = TestArgs::class)
          fun TestScreen2(
            navArgs: TestArgs
          ) {}
          """.trimIndent()
            )
        )

        val result = compilation.compile()

        assertEquals(result.exitCode, KotlinCompilation.ExitCode.OK)
        assertTrue(
            compilation.kspSourcesDir.walkTopDown()
                .filter { it.nameWithoutExtension == "TestScreen1Destination" || it.nameWithoutExtension == "TestScreen2Destination" }
                .toList().isNotEmpty()
        )
    }

    @Test
    fun `@Destination with navArgsDelegate annotated serializable typealias`() {
        val compilation = prepareCompilation(
            kotlin(
                "Screen.kt",
                """
          package com.ramcosta.composedestinations.example

          import com.ramcosta.composedestinations.annotation.Destination
          import com.ramcosta.composedestinations.annotation.RootNavGraph
          import kotlinx.serialization.Serializable

          data class IsSerializable(
            val color: String
          )

          typealias Aliased = @Serializable IsSerializable

          data class TestArgs(
            val arg: Aliased
          )

          @RootNavGraph(start = true)
          @Destination(route = "test1")
          fun TestScreen1() {}

          @Destination(route = "test2", navArgsDelegate = TestArgs::class)
          fun TestScreen2(
            navArgs: TestArgs
          ) {}
          """.trimIndent()
            )
        )

        val result = compilation.compile()

        assertEquals(result.exitCode, KotlinCompilation.ExitCode.OK)
        assertTrue(
            compilation.kspSourcesDir.walkTopDown()
                .filter { it.nameWithoutExtension == "TestScreen1Destination" || it.nameWithoutExtension == "TestScreen2Destination" }
                .toList().isNotEmpty()
        )
    }

    @Test
    fun `@Destination with navArgsDelegate simple typealias`() {
        val compilation = prepareCompilation(
            kotlin(
                "Screen.kt",
                """
          package com.ramcosta.composedestinations.example

          import com.ramcosta.composedestinations.annotation.Destination
          import com.ramcosta.composedestinations.annotation.RootNavGraph

          typealias Aliased = String

          data class TestArgs(
            val arg: Aliased
          )

          @RootNavGraph(start = true)
          @Destination(route = "test1")
          fun TestScreen1() {}

          @Destination(route = "test2", navArgsDelegate = TestArgs::class)
          fun TestScreen2(
            navArgs: TestArgs
          ) {}
          """.trimIndent()
            )
        )

        val result = compilation.compile()

        assertEquals(result.exitCode, KotlinCompilation.ExitCode.OK)
        assertTrue(
            compilation.kspSourcesDir.walkTopDown()
                .filter { it.nameWithoutExtension == "TestScreen1Destination" || it.nameWithoutExtension == "TestScreen2Destination" }
                .toList().isNotEmpty()
        )
    }

    @Test
    fun `@Destination with navArgsDelegate double typealias`() {
        val compilation = prepareCompilation(
            kotlin(
                "Screen.kt",
                """
          package com.ramcosta.composedestinations.example

          import com.ramcosta.composedestinations.annotation.Destination
          import com.ramcosta.composedestinations.annotation.RootNavGraph

          typealias Aliased = String
          typealias AliasedSecond = Aliased

          data class TestArgs(
            val arg: AliasedSecond
          )

          @RootNavGraph(start = true)
          @Destination(route = "test1")
          fun TestScreen1() {}

          @Destination(route = "test2", navArgsDelegate = TestArgs::class)
          fun TestScreen2(
            navArgs: TestArgs
          ) {}
          """.trimIndent()
            )
        )

        val result = compilation.compile()

        assertEquals(result.exitCode, KotlinCompilation.ExitCode.OK)
        assertTrue(
            compilation.kspSourcesDir.walkTopDown()
                .filter { it.nameWithoutExtension == "TestScreen1Destination" || it.nameWithoutExtension == "TestScreen2Destination" }
                .toList().isNotEmpty()
        )
    }

    @Test
    fun `@Destination with navArgsDelegate from different module`() {
        val compilation = prepareCompilation(
            kotlin(
                "Screen.kt",
                """
          package com.ramcosta.composedestinations.example

          import com.ramcosta.composedestinations.annotation.Destination
          import com.ramcosta.composedestinations.annotation.RootNavGraph
          import com.ramcosta.playground.core.BlogPostArgs

          @RootNavGraph(start = true)
          @Destination(route = "test1")
          fun TestScreen1() {}

          @Destination(route = "test2", navArgsDelegate = BlogPostArgs::class)
          fun TestScreen2(
            navArgs: BlogPostArgs
          ) {}
          """.trimIndent()
            )
        )

        val result = compilation.compile()

        assertEquals(result.exitCode, KotlinCompilation.ExitCode.OK)
        assertTrue(
            "Files wasn't generated",
            compilation.kspSourcesDir.walkTopDown()
                .filter { it.nameWithoutExtension == "TestScreen1Destination" || it.nameWithoutExtension == "TestScreen2Destination" }
                .toList().isNotEmpty()
        )
    }

    @Test
    fun `@Destination with navArgsDelegate from different module with default values`() {
        val compilation = prepareCompilation(
            kotlin(
                "Screen.kt",
                """
          package com.ramcosta.composedestinations.example

          import com.ramcosta.composedestinations.annotation.Destination
          import com.ramcosta.composedestinations.annotation.RootNavGraph
          import com.ramcosta.playground.core.WithDefaultValueArgs

          @RootNavGraph(start = true)
          @Destination(route = "test1")
          fun TestScreen1() {}

          @Destination(route = "test2", navArgsDelegate = WithDefaultValueArgs::class)
          fun TestScreen2(
            navArgs: WithDefaultValueArgs
          ) {}
          """.trimIndent()
            )
        )

        val result = compilation.compile()

        assertEquals(result.exitCode, KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertTrue(result.messages.contains("com.ramcosta.composedestinations.codegen.commons.IllegalDestinationsSetup: Cannot detect default value for navigation argument 'isCreate'"))
    }

    private fun prepareCompilation(vararg sourceFiles: SourceFile): KotlinCompilation {
        return KotlinCompilation().apply {
            workingDir = temporaryFolder.root
            inheritClassPath = true
            symbolProcessorProviders = listOf(ProcessorProvider())
            sources = sourceFiles.asList()
            verbose = false
            kspIncremental = false
        }
    }

    private fun compile(vararg sourceFiles: SourceFile): KotlinCompilation.Result {
        return prepareCompilation(*sourceFiles).compile()
    }
}
