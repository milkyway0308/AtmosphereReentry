package skywolf46.atmospherereentry.packetbridge.serializers

import io.netty.buffer.ByteBuf
import skywolf46.atmospherereentry.api.packetbridge.DataSerializerBase
import skywolf46.atmospherereentry.api.packetbridge.annotations.NetworkSerializer

@NetworkSerializer
class ByteArraySerializer : DataSerializerBase<ByteArray>(){
    override fun serialize(buf: ByteBuf, dataBase: ByteArray) {
        buf.writeInt(dataBase.size)
        buf.writeBytes(dataBase)
    }

    override fun deserialize(buf: ByteBuf): ByteArray {
        return ByteArray(buf.readInt()).apply {
            buf.readBytes(this)
        }
    }

}