package com.ramcosta.composedestinations.navargs.ktxserializable

import android.util.Base64
import com.ramcosta.samples.playground.ui.screens.navargs.ktxserializable.DefaultKtxSerializableNavTypeSerializer
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import org.junit.After
import org.junit.Assert.*
import org.junit.Before

import org.junit.Test

import java.util.Base64 as JBase64

@ExperimentalSerializationApi
class DefaultKtxSerializableNavTypeSerializerTest {

    @Before
    fun setup() {
        mockkStatic("android.util.Base64")
        val flags = Base64.URL_SAFE or Base64.NO_WRAP
        every { Base64.encodeToString(any(), flags) } answers { call ->
            JBase64.getEncoder().encodeToString(call.invocation.args.first() as ByteArray)
        }
        every { Base64.decode(any<ByteArray>(), flags) } answers { call ->
            JBase64.getDecoder().decode(call.invocation.args.first() as ByteArray)
        }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `should parse value to string`() {
        // Arrange
        val serializer = DefaultKtxSerializableNavTypeSerializer(
            serializer = TestSerializable.serializer(),
        )

        // Act
        val routeString = serializer.toRouteString(TestSerializable())

        // Assert
        assertFalse(routeString.isBlank())
        assertEquals("eyJhbkFycmF5IjpbMSwyLDNdfQ==", routeString)
    }

    @Test
    fun `should parse string to value`() {
        // Arrange
        val serializer = DefaultKtxSerializableNavTypeSerializer(
            serializer = TestSerializable.serializer(),
        )
        val base64 = "eyJhbkFycmF5IjpbMSwyLDNdfQ=="

        // Act
        val serializable = serializer.fromRouteString(base64)

        // Assert
        assertEquals(TestSerializable(), serializable)
    }

    @Test
    fun `should encode and decode to route string a data class with value class property`() {
        // Arrange
        val serializer = DefaultKtxSerializableNavTypeSerializer(
            serializer = TestValueClassProperty.serializer(),
        )
        val password = Password("that is a value")
        val expected = TestValueClassProperty(
            name = "testing name",
            password = password,
        )

        // Act
        val routeString = serializer.toRouteString(expected)
        val actual = serializer.fromRouteString(routeString)

        // Assert
        assertEquals(password.toMask(), actual.password.toMask())
        assertEquals(password.value, actual.password.value)
        assertEquals(password, actual.password)
        assertEquals(expected, actual)
    }
}

// region Data classes
@Serializable
data class TestSerializable(
    val aInt: Int = 1,
    val aString: String = "testing",
    val aFloat: Float = 100.5F,
    val aDouble: Double = 1.5,
    val anArray: IntArray = intArrayOf(1, 2, 3),
    val aList: List<Int> = listOf(100, 200, 300),
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TestSerializable

        if (aInt != other.aInt) return false
        if (aString != other.aString) return false
        if (aFloat != other.aFloat) return false
        if (aDouble != other.aDouble) return false
        if (!anArray.contentEquals(other.anArray)) return false
        if (aList != other.aList) return false

        return true
    }

    override fun hashCode(): Int {
        var result = aInt
        result = 31 * result + aString.hashCode()
        result = 31 * result + aFloat.hashCode()
        result = 31 * result + aDouble.hashCode()
        result = 31 * result + anArray.contentHashCode()
        result = 31 * result + aList.hashCode()
        return result
    }
}

@Serializable
data class TestValueClassProperty(
    val name: String,
    val password: Password,
)

@JvmInline
@Serializable
value class Password(val value: String) {
    fun toMask() = "*".repeat(value.length)
}

// endregion Data classes
