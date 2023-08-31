package skywolf46.atmospherereentry.packetbridge.serializers

import io.netty.buffer.ByteBuf
import skywolf46.atmospherereentry.api.packetbridge.DataSerializerBase
import skywolf46.atmospherereentry.api.packetbridge.annotations.NetworkSerializer
import skywolf46.atmospherereentry.api.packetbridge.data.DoubleHashedType
import skywolf46.atmospherereentry.api.packetbridge.util.deserializeAs
import skywolf46.atmospherereentry.api.packetbridge.util.serializeTo

@NetworkSerializer
class ListSerializer : DataSerializerBase<ArrayList<*>>() {
    override fun serialize(buf: ByteBuf, dataBase: ArrayList<*>) {
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

    override fun deserialize(buf: ByteBuf): ArrayList<*> {
        val size = buf.readInt()
        val arr = arrayListOf<Any?>()
        for (x in 0 until size) {
            if (buf.readByte().toInt() == 0) {
                arr.add(null)
            } else {
                val type = DoubleHashedType(buf.readInt() to buf.readInt())
                arr.add(buf.deserializeAs(type))
            }
        }
        return arr
    }
}