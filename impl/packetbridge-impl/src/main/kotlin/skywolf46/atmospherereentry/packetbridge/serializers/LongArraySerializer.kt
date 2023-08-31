package skywolf46.atmospherereentry.packetbridge.serializers

import io.netty.buffer.ByteBuf
import skywolf46.atmospherereentry.api.packetbridge.DataSerializerBase
import skywolf46.atmospherereentry.api.packetbridge.annotations.NetworkSerializer

@NetworkSerializer
class LongArraySerializer : DataSerializerBase<LongArray>() {
    override fun serialize(buf: ByteBuf, dataBase: LongArray) {
        buf.writeInt(dataBase.size)
        dataBase.forEach {
            buf.writeLong(it)
        }
    }

    override fun deserialize(buf: ByteBuf): LongArray {
        return LongArray(buf.readInt()) {
            buf.readLong()
        }
    }

}