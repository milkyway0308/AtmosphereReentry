package skywolf46.atmospherereentry.packetbridge.serializers

import io.netty.buffer.ByteBuf
import skywolf46.atmospherereentry.api.packetbridge.DataSerializerBase
import skywolf46.atmospherereentry.api.packetbridge.annotations.NetworkSerializer
import skywolf46.atmospherereentry.api.packetbridge.data.DoubleHashedType
import skywolf46.atmospherereentry.api.packetbridge.util.deserializeAs
import skywolf46.atmospherereentry.api.packetbridge.util.serializeTo

@NetworkSerializer
class LinkedHashMapSerializer : DataSerializerBase<LinkedHashMap<*, *>>() {

    override fun serialize(buf: ByteBuf, dataBase: LinkedHashMap<*, *>) {
        buf.writeInt(dataBase.size)
        dataBase.forEach { (k, v) ->
            DoubleHashedType(k!!::class.java).serializeTo(buf)
            k.serializeTo(buf)
            DoubleHashedType(v!!::class.java).serializeTo(buf)
            v.serializeTo(buf)
        }
    }

    override fun deserialize(buf: ByteBuf): LinkedHashMap<*, *> {
        val size = buf.readInt()
        val map = LinkedHashMap<Any?, Any?>()
        for (x in 0 until size) {
            val keyType = DoubleHashedType(buf.readInt() to buf.readInt())
            val key = buf.deserializeAs<Any>(keyType)
            val valueType = DoubleHashedType(buf.readInt() to buf.readInt())
            val value = buf.deserializeAs<Any>(valueType)
            map[key] = value
        }
        return map
    }
}