package skywolf46.atmospherereentry.packetbridge.serializers

import io.netty.buffer.ByteBuf
import skywolf46.atmospherereentry.api.packetbridge.DataSerializerBase
import skywolf46.atmospherereentry.api.packetbridge.annotations.NetworkSerializer

@NetworkSerializer
class DoubleArraySerializer : DataSerializerBase<DoubleArray>() {
    override fun serialize(buf: ByteBuf, dataBase: DoubleArray) {
        buf.writeInt(dataBase.size)
        dataBase.forEach {
            buf.writeDouble(it)
        }
    }

    override fun deserialize(buf: ByteBuf): DoubleArray {
        return DoubleArray(buf.readInt()) {
            buf.readDouble()
        }
    }
}