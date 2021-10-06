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
        )
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