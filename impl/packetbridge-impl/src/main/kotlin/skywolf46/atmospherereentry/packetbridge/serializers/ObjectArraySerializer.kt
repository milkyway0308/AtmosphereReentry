package skywolf46.atmospherereentry.packetbridge.serializers

import io.netty.buffer.ByteBuf
import skywolf46.atmospherereentry.api.packetbridge.DataSerializerBase
import skywolf46.atmospherereentry.api.packetbridge.annotations.NetworkSerializer
import skywolf46.atmospherereentry.api.packetbridge.data.DoubleHashedType
import skywolf46.atmospherereentry.api.packetbridge.util.deserializeAs
import skywolf46.atmospherereentry.api.packetbridge.util.readString
import skywolf46.atmospherereentry.api.packetbridge.util.serializeTo
import skywolf46.atmospherereentry.api.packetbridge.util.writeString

@NetworkSerializer
class ObjectArraySerializer : DataSerializerBase<Array<*>>() {
    override fun serialize(buf: ByteBuf, dataBase: Array<*>) {
        buf.writeString(dataBase.javaClass.componentType.name)
        buf.writeInt(dataBase.size)
        dataBase.forEach {
            if (it == null) {
                buf.writeByte(0)
            } else {
                buf.writeByte(1)
                DoubleHashedType(it.javaClass).serializeTo(buf)
                it.serializeTo(buf)
            }
        }
    }

    override fun deserialize(buf: ByteBuf): Array<*> {
        val arrayClass = Class.forName(buf.readString())
        val size = buf.readInt()
        val arr = java.lang.reflect.Array.newInstance(arrayClass, size) as Array<Any?>
        for (x in 0 until size) {
            if (buf.readByte().toInt() == 0) {
                arr[x] = null
            } else {
                val type = DoubleHashedType(buf.readInt() to buf.readInt())
                arr[x] = buf.deserializeAs(type)
            }
        }
        return arr
    }
}