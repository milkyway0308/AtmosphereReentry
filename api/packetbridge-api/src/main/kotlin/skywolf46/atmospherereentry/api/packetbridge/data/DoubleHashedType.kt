package skywolf46.atmospherereentry.api.packetbridge.data

import skywolf46.atmospherereentry.api.packetbridge.annotations.CoreType
import skywolf46.atmospherereentry.api.packetbridge.util.HashUtil
import kotlin.reflect.KClass

@CoreType(Short.MIN_VALUE)
class DoubleHashedType(val hash: Pair<Int, Int>) {
    constructor(cls: Class<out Any>) : this(
        if (cls.isArray && !cls.componentType().isPrimitive) "_Array" else cls.name
    )

    constructor(kls: KClass<out Any>) : this(kls.java)

    constructor(string: String) : this(HashUtil.getDoubleHash(string))

    override fun equals(other: Any?): Boolean {
        return other is DoubleHashedType && other.hash == this.hash
    }

    override fun hashCode(): Int {
        return arrayOf(hash.first, hash.second).contentHashCode()
    }

    override fun toString(): String {
        return "DoubleHashedType(hash=${hashCode()})"
    }
}