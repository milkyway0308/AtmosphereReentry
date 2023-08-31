package skywolf46.atmospherereentry.packetbridge.serializers

import io.netty.buffer.ByteBuf
import skywolf46.atmospherereentry.api.packetbridge.DataSerializerBase
import skywolf46.atmospherereentry.api.packetbridge.annotations.ReflectedSerializer

@ReflectedSerializer
class ShortArraySerializer : DataSerializerBase<ShortArray>(){
    override fun serialize(buf: ByteBuf, dataBase: ShortArray) {
        buf.writeInt(dataBase.size)
        dataBase.forEach {
            buf.writeShort(it.toInt())
        }
    }

    override fun deserialize(buf: ByteBuf): ShortArray {
        return ShortArray(buf.readInt()) {
            buf.readShort()
        }
    }

}