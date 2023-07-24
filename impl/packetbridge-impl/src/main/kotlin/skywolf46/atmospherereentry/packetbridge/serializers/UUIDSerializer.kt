package skywolf46.atmospherereentry.packetbridge.serializers

import io.netty.buffer.ByteBuf
import skywolf46.atmospherereentry.api.packetbridge.DataSerializerBase
import skywolf46.atmospherereentry.api.packetbridge.annotations.NetworkSerializer
import java.util.*

@NetworkSerializer
class UUIDSerializer : DataSerializerBase<UUID>() {
    override fun serialize(buf: ByteBuf, dataBase: UUID) {
        buf.writeLong(dataBase.mostSignificantBits)
        buf.writeLong(dataBase.leastSignificantBits)
    }

    override fun deserialize(buf: ByteBuf): UUID {
        return UUID(buf.readLong(), buf.readLong())
    }
}