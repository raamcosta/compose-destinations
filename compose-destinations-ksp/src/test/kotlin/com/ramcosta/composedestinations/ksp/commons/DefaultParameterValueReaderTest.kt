package com.ramcosta.composedestinations.ksp.commons

import org.junit.Test

class DefaultParameterValueReaderTest {

    private val objectUnderTest = DefaultParameterValueReader

    private val casesToTest = arrayOf(
        TestCase(
            lineText = "    arg1: String? = \"defaultArg\") {",
            argName = "arg1",
            argType = "String",
            expected = "\"defaultArg\""
        ),
        TestCase(
            lineText = "    arg1: String? = \"defaultArg\"",
            argName = "arg1",
            argType = "String",
            expected = "\"defaultArg\""
        ),
        TestCase(
            lineText = "    arg1: String? = \"defaultArg\",",
            argName = "arg1",
            argType = "String",
            expected = "\"defaultArg\""
        ),
        TestCase(
            lineText = "arg1: String = \"multiple words string\",",
            argName = "arg1",
            argType = "String",
            expected = "\"multiple words string\""
        ),
        TestCase(
            lineText = "arg1: String = \"multiple, words string\", arg2: String = \"doesn't matter\"",
            argName = "arg1",
            argType = "String",
            expected = "\"multiple, words string\""
        ),
        TestCase(
            lineText = "arg1: String = \"multiple, \\\"words string\", arg2: String = \"doesn't matter\"",
            argName = "arg1",
            argType = "String",
            expected = "\"multiple, \\\"words string\""
        ),
        TestCase(
            lineText = "arg1: String = \"doesn't matter\", arg2: String = \"mul\\\"tiple words \\\"string\"",
            argName = "arg2",
            argType = "String",
            expected = "\"mul\\\"tiple words \\\"string\""
        ),
        TestCase(
            lineText = "arg1: String = \"doesn't matter\", arg2: Int = 2, arg3: String = \"mul\\\"tiple words \\\"string\"",
            argName = "arg3",
            argType = "String",
            expected = "\"mul\\\"tiple words \\\"string\""
        ),
        TestCase(
            lineText = "arg1: String = \"doesn't matter\", arg2: Int = 2, arg3: String = \"mul\\\"tiple words \\\"string\"",
            argName = "arg2",
            argType = "Int",
            expected = "2"
        ),
    )

    @Test
    fun testCases() {
        casesToTest.forEach {
            val result = objectUnderTest.readDefaultValue(
                it.lineText,
                it.argName,
                it.argType
            )

            assert(result == it.expected)
        }
    }

    class TestCase(
        val lineText: String,
        val argName: String,
        val argType: String,
        val expected: String?
    )
}