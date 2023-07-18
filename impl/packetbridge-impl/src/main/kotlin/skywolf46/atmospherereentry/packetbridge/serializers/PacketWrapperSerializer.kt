package skywolf46.atmospherereentry.packetbridge.serializers

import io.netty.buffer.ByteBuf
import skywolf46.atmospherereentry.api.packetbridge.DataSerializerBase
import skywolf46.atmospherereentry.api.packetbridge.PacketBase
import skywolf46.atmospherereentry.api.packetbridge.annotations.NetworkSerializer
import skywolf46.atmospherereentry.api.packetbridge.data.PacketWrapper
import skywolf46.atmospherereentry.api.packetbridge.util.deserializeRoot
import skywolf46.atmospherereentry.api.packetbridge.util.serializeRootTo
import skywolf46.atmospherereentry.api.packetbridge.util.serializeTo
import java.util.*

@NetworkSerializer
class PacketWrapperSerializer : DataSerializerBase<PacketWrapper<*>>{
    override fun serialize(buf: ByteBuf, dataBase: PacketWrapper<*>) {
        buf.writeLong(dataBase.packetId)
        buf.writeLong(dataBase.from.mostSignificantBits)
        buf.writeLong(dataBase.from.leastSignificantBits)
        dataBase.packet.serializeRootTo(buf)
    }

    override fun deserialize(buf: ByteBuf): PacketWrapper<*> {
        val packetId = buf.readLong()
        val from = UUID(buf.readLong(), buf.readLong())
        val packet = buf.deserializeRoot<PacketBase>()
        return PacketWrapper(packet, from, packetId)
    }
}