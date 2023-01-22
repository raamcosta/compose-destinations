package com.ramcosta.composedestinations.ksp.commons

import com.ramcosta.composedestinations.codegen.model.DefaultValue
import org.junit.Test

class DefaultParameterValueReaderTest {

    private val objectUnderTest = DefaultParameterValueReader

    private val samePackageResolver: (String, String) -> ResolvedSymbol? = { pckg, _ ->
        if (pckg == "com.ramcosta.composedestinations.ksp.commons") {
            ResolvedSymbol(true)
        } else null
    }

    private val casesToTest = arrayOf(
        TestCase(
            lineText = "    arg1: String? = \"defaultArg\") {",
            argName = "arg1",
            argType = "String",
            expected = DefaultValue("\"defaultArg\"")
        ),
        TestCase(
            lineText = "    arg1: String? = null,",
            argName = "arg1",
            argType = "String",
            expected = DefaultValue("null")
        ),
        TestCase(
            lineText = "internal fun EditRitual(nav: RespawnNavigator, ritualId: Uuid? = null) = AnalyticsScreen(\"EditRitual\") {",
            argName = "ritualId",
            argType = "Uuid",
            imports = listOf("com.ramcosta.package.Uuid"),
            expected = DefaultValue("null", listOf())
        ),
        TestCase(
            lineText = "    arg1: String? = \"defaultArg\"",
            argName = "arg1",
            argType = "String",
            expected = DefaultValue("\"defaultArg\"")
        ),
        TestCase(
            lineText = "    arg1: String? = \"defaultArg\",",
            argName = "arg1",
            argType = "String",
            expected = DefaultValue("\"defaultArg\"")
        ),
        TestCase(
            lineText = "arg1: String = \"multiple words string\",",
            argName = "arg1",
            argType = "String",
            expected = DefaultValue("\"multiple words string\"")
        ),
        TestCase(
            lineText = "arg1: String = \"multiple, words string\", arg2: String = \"doesn't matter\"",
            argName = "arg1",
            argType = "String",
            expected = DefaultValue("\"multiple, words string\"")
        ),
        TestCase(
            lineText = "arg1: String = \"multiple, \\\"words string\", arg2: String = \"doesn't matter\"",
            argName = "arg1",
            argType = "String",
            expected = DefaultValue("\"multiple, \\\"words string\"")
        ),
        TestCase(
            lineText = "arg1: String = \"doesn't matter\", arg2: String = \"mul\\\"tiple words \\\"string\"",
            argName = "arg2",
            argType = "String",
            expected = DefaultValue("\"mul\\\"tiple words \\\"string\"")
        ),
        TestCase(
            lineText = "arg1: String = \"doesn't matter\", arg2: Int = 2, arg3: String = \"mul\\\"tiple words \\\"string\"",
            argName = "arg3",
            argType = "String",
            expected = DefaultValue("\"mul\\\"tiple words \\\"string\"")
        ),
        TestCase(
            lineText = "arg1: String = \"doesn't matter\", arg2: Int = 2, arg3: String = \"mul\\\"tiple words \\\"string\"",
            argName = "arg2",
            argType = "Int",
            expected = DefaultValue("2")
        ),
        TestCase(
            lineText = "arg1: Float = 123.0f",
            argName = "arg1",
            argType = "Float",
            expected = DefaultValue("123.0f")
        ),
        TestCase(
            lineText = "arg1: Float = 123L",
            argName = "arg1",
            argType = "Float",
            expected = DefaultValue("123L")
        ),
        TestCase(
            lineText = "arg1: Boolean = true",
            argName = "arg1",
            argType = "Boolean",
            expected = DefaultValue("true")
        ),


        // Same package public symbols


        TestCase(
            isResolvable = samePackageResolver,
            lineText = "arg1: String = defaultValue()",
            argName = "arg1",
            argType = "String",
            imports = emptyList(),
            expected = DefaultValue("defaultValue()", listOf("com.ramcosta.composedestinations.ksp.commons.defaultValue"))
        ),
        TestCase(
            isResolvable = samePackageResolver,
            lineText = "arg1: String = Stuff.defaultValue()",
            argName = "arg1",
            argType = "String",
            imports = emptyList(),
            expected = DefaultValue("Stuff.defaultValue()", listOf("com.ramcosta.composedestinations.ksp.commons.Stuff"))
        ),
        TestCase(
            isResolvable = samePackageResolver,
            lineText = "arg1: String = Outer.Stuff.defaultValue()",
            argName = "arg1",
            argType = "String",
            imports = emptyList(),
            expected = DefaultValue("Outer.Stuff.defaultValue()", listOf("com.ramcosta.composedestinations.ksp.commons.Outer"))
        ),
        TestCase(
            isResolvable = samePackageResolver,
            lineText = "arg1: String = Stuff.defaultValue",
            argName = "arg1",
            argType = "String",
            imports = emptyList(),
            expected = DefaultValue("Stuff.defaultValue", listOf("com.ramcosta.composedestinations.ksp.commons.Stuff"))
        ),
        TestCase(
            isResolvable = samePackageResolver,
            lineText = "arg1: String = defaultValue",
            argName = "arg1",
            argType = "String",
            imports = emptyList(),
            expected = DefaultValue("defaultValue", listOf("com.ramcosta.composedestinations.ksp.commons.defaultValue"))
        ),
        TestCase(
            isResolvable = samePackageResolver,
            lineText = "arg1: String = Stuff.asd().stuff()",
            argName = "arg1",
            argType = "String",
            imports = emptyList(),
            expected = DefaultValue("Stuff.asd().stuff()", listOf("com.ramcosta.composedestinations.ksp.commons.Stuff"))
        ),
        TestCase(
            isResolvable = samePackageResolver,
            lineText = "arg1: String = Stuff.asd().stuff",
            argName = "arg1",
            argType = "String",
            imports = emptyList(),
            expected = DefaultValue("Stuff.asd().stuff", listOf("com.ramcosta.composedestinations.ksp.commons.Stuff"))
        ),


        // other package public symbols (no .* imports)


        TestCase(
            lineText = "arg1: String = defaultValue()",
            argName = "arg1",
            argType = "String",
            imports = listOf("com.ramcosta.package.defaultValue"),
            expected = DefaultValue("defaultValue()", listOf("com.ramcosta.package.defaultValue"))
        ),
        TestCase(
            lineText = "arg1: String = Outer.Stuff.defaultValue()",
            argName = "arg1",
            argType = "String",
            imports = listOf("com.ramcosta.package.Outer"),
            expected = DefaultValue("Outer.Stuff.defaultValue()", listOf("com.ramcosta.package.Outer"))
        ),
        TestCase(
            lineText = "arg1: String = defaultValue",
            argName = "arg1",
            argType = "String",
            imports = listOf("com.ramcosta.package.defaultValue"),
            expected = DefaultValue("defaultValue", listOf("com.ramcosta.package.defaultValue"))
        ),
        TestCase(
            lineText = "arg1: String = Stuff.asd().stuff()",
            argName = "arg1",
            argType = "String",
            imports = listOf("com.ramcosta.package.Stuff"),
            expected = DefaultValue("Stuff.asd().stuff()", listOf("com.ramcosta.package.Stuff"))
        ),
        TestCase(
            lineText = "arg1: String = Stuff.asd().stuff",
            argName = "arg1",
            argType = "String",
            imports = listOf("com.ramcosta.package.Stuff"),
            expected = DefaultValue("Stuff.asd().stuff", listOf("com.ramcosta.package.Stuff"))
        ),
        TestCase(
            isResolvable = { pckg, _ -> if (pckg == "com.ramcosta.package") ResolvedSymbol(true) else null },
            lineText = "arg1: String = Stuff.defaultValue",
            argName = "arg1",
            argType = "String",
            imports = listOf("com.ramcosta.package.*"),
            expected = DefaultValue("Stuff.defaultValue", listOf("com.ramcosta.package.*"))
        ),
    )

    @Test
    fun testCases() {
        casesToTest.forEachIndexed { idx, it ->
            println("Testing case #$idx: ${it.lineText}")
            val result = objectUnderTest.readDefaultValue(
                it.isResolvable,
                it.lineText,
                "com.ramcosta.composedestinations.ksp.commons",
                it.imports,
                it.argName,
                it.argType
            )

            assert(result == it.expected) {
                "\nexpected= ${it.expected} \ngot= $result"
            }
        }
    }

    class TestCase(
        val isResolvable: (String, String) -> ResolvedSymbol? = { _, _ -> null },
        val lineText: String,
        val argName: String,
        val argType: String,
        val imports: List<String> = emptyList(),
        val expected: DefaultValue?
    )
}