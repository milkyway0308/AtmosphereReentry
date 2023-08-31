package skywolf46.atmospherereentry.packetbridge.serializers

import io.netty.buffer.ByteBuf
import skywolf46.atmospherereentry.api.packetbridge.DataSerializerBase
import skywolf46.atmospherereentry.api.packetbridge.annotations.NetworkSerializer

@NetworkSerializer
class IntArraySerializer : DataSerializerBase<IntArray>() {
    override fun serialize(buf: ByteBuf, dataBase: IntArray) {
        buf.writeInt(dataBase.size)
        dataBase.forEach {
            buf.writeInt(it)
        }
    }

    override fun deserialize(buf: ByteBuf): IntArray {
        return IntArray(buf.readInt()) {
            buf.readInt()
        }
    }

}