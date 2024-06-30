package com.ramcosta.composedestinations.result

import com.ramcosta.composedestinations.annotation.internal.InternalDestinationsApi
import com.ramcosta.composedestinations.navargs.DestinationsNavType
import com.ramcosta.composedestinations.navargs.primitives.DestinationsBooleanNavType
import com.ramcosta.composedestinations.navargs.primitives.DestinationsFloatNavType
import com.ramcosta.composedestinations.navargs.primitives.DestinationsIntNavType
import com.ramcosta.composedestinations.navargs.primitives.DestinationsLongNavType
import com.ramcosta.composedestinations.navargs.primitives.DestinationsStringNavType
import com.ramcosta.composedestinations.navargs.primitives.array.DestinationsBooleanArrayNavType
import com.ramcosta.composedestinations.navargs.primitives.array.DestinationsFloatArrayNavType
import com.ramcosta.composedestinations.navargs.primitives.array.DestinationsIntArrayNavType
import com.ramcosta.composedestinations.navargs.primitives.array.DestinationsLongArrayNavType
import com.ramcosta.composedestinations.navargs.primitives.array.DestinationsStringArrayNavType
import com.ramcosta.composedestinations.navargs.primitives.arraylist.DestinationsBooleanArrayListNavType
import com.ramcosta.composedestinations.navargs.primitives.arraylist.DestinationsFloatArrayListNavType
import com.ramcosta.composedestinations.navargs.primitives.arraylist.DestinationsIntArrayListNavType
import com.ramcosta.composedestinations.navargs.primitives.arraylist.DestinationsLongArrayListNavType
import com.ramcosta.composedestinations.navargs.primitives.arraylist.DestinationsStringArrayListNavType
import kotlin.reflect.KClass

internal val navTypeRegistry: MutableMap<TypeInfo, DestinationsNavType<*>> = mutableMapOf(
    Boolean::class.simple to DestinationsBooleanNavType,
    Float::class.simple to DestinationsFloatNavType,
    Int::class.simple to DestinationsIntNavType,
    Long::class.simple to DestinationsLongNavType,
    String::class.simple to DestinationsStringNavType,

    BooleanArray::class.simple to DestinationsBooleanArrayNavType,
    FloatArray::class.simple to DestinationsFloatArrayNavType,
    IntArray::class.simple to DestinationsIntArrayNavType,
    LongArray::class.simple to DestinationsLongArrayNavType,
    Array::class.with(String::class) to DestinationsStringArrayNavType,

    ArrayList::class.with(Boolean::class) to DestinationsBooleanArrayListNavType,
    ArrayList::class.with(Float::class) to DestinationsFloatArrayListNavType,
    ArrayList::class.with(Int::class) to DestinationsIntArrayListNavType,
    ArrayList::class.with(Long::class) to DestinationsLongArrayListNavType,
    ArrayList::class.with(String::class) to DestinationsStringArrayListNavType,
)

@InternalDestinationsApi
fun registerComplexNavType(typeInfo: TypeInfo, navType: DestinationsNavType<*>) {
    navTypeRegistry[typeInfo] = navType
}

@InternalDestinationsApi
data class TypeInfo(
    val type: KClass<*>,
    val typeArgs: Array<KClass<*>>? = null
)  {

    override fun toString(): String {
        return "${type.qualifiedName}${typeArgs?.joinToString { it.toString() } ?: ""}"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TypeInfo

        if (type != other.type) return false
        if (typeArgs != null) {
            if (other.typeArgs == null) return false
            if (!typeArgs.contentEquals(other.typeArgs)) return false
        } else if (other.typeArgs != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + (typeArgs?.contentHashCode() ?: 0)
        return result
    }
}

private fun KClass<*>.with(typeArg: KClass<*>) = TypeInfo(this, arrayOf(typeArg))
private val KClass<*>.simple get() = TypeInfo(this)