package skywolf46.atmospherereentry.packetbridge.serializers

import io.netty.buffer.ByteBuf
import skywolf46.atmospherereentry.api.packetbridge.DataSerializerBase
import skywolf46.atmospherereentry.api.packetbridge.annotations.NetworkSerializer

@NetworkSerializer
class FloatArraySerializer : DataSerializerBase<FloatArray>() {
    override fun serialize(buf: ByteBuf, dataBase: FloatArray) {
        buf.writeInt(dataBase.size)
        dataBase.forEach {
            buf.writeFloat(it)
        }
    }

    override fun deserialize(buf: ByteBuf): FloatArray {
        return FloatArray(buf.readInt()) {
            buf.readFloat()
        }
    }
}