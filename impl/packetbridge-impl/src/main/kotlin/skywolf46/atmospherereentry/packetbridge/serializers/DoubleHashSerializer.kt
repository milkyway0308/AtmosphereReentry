package skywolf46.atmospherereentry.packetbridge.serializers

import io.netty.buffer.ByteBuf
import skywolf46.atmospherereentry.api.packetbridge.DataSerializerBase
import skywolf46.atmospherereentry.api.packetbridge.annotations.NetworkSerializer
import skywolf46.atmospherereentry.api.packetbridge.data.DoubleHashedType

@NetworkSerializer
class DoubleHashSerializer : DataSerializerBase<DoubleHashedType>(){
    override fun serialize(buf: ByteBuf, dataBase: DoubleHashedType) {
        buf.writeInt(dataBase.hash.first)
        buf.writeInt(dataBase.hash.second)
    }

    override fun deserialize(buf: ByteBuf): DoubleHashedType {
        return DoubleHashedType(buf.readInt() to buf.readInt())
    }
}