package skywolf46.atmospherereentry.api.packetbridge.data

import io.netty.buffer.ByteBuf
import skywolf46.atmospherereentry.api.packetbridge.DataSerializerBase
import skywolf46.atmospherereentry.api.packetbridge.annotations.CoreType
import skywolf46.atmospherereentry.api.packetbridge.annotations.NetworkSerializer
import skywolf46.atmospherereentry.api.packetbridge.util.HashUtil
import kotlin.reflect.KClass

@CoreType(Short.MIN_VALUE)
class DoubleHashedType(val hash: Pair<Int, Int>) {
    constructor(cls: Class<out Any>) : this(HashUtil.getDoubleHash(cls.name))

    constructor(kls: KClass<out Any>) : this(HashUtil.getDoubleHash(kls.java.name))

    constructor(string: String) : this(HashUtil.getDoubleHash(string))

    override fun equals(other: Any?): Boolean {
        return other is DoubleHashedType && other.hash == this.hash
    }

    override fun hashCode(): Int {
        return hash.hashCode()
    }
}