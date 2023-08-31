package skywolf46.atmospherereentry.api.packetbridge.packets.server

import skywolf46.atmospherereentry.api.packetbridge.PacketBase
import skywolf46.atmospherereentry.api.packetbridge.annotations.ReflectedSerializer
import skywolf46.atmospherereentry.api.packetbridge.util.deserializeAs
import skywolf46.atmospherereentry.api.packetbridge.util.useByteBuf
import java.util.UUID

@ReflectedSerializer
class PacketRelay(val packet: ByteArray) : PacketBase {
    fun asOriginalPacket(): PacketBase {
        return useByteBuf { buf ->
            buf.writeBytes(packet)
            buf.deserializeAs()
        }
    }
}