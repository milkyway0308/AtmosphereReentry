package skywolf46.atmospherereentry.api.packetbridge.packets.client

import io.netty.buffer.ByteBufAllocator
import skywolf46.atmospherereentry.api.packetbridge.PacketBase
import skywolf46.atmospherereentry.api.packetbridge.ReflectedSerializerBase
import skywolf46.atmospherereentry.api.packetbridge.annotations.ReflectedSerializer
import skywolf46.atmospherereentry.api.packetbridge.util.deserializeAs
import skywolf46.atmospherereentry.api.packetbridge.util.useByteBuf
import skywolf46.atmospherereentry.api.packetbridge.util.writeString
import java.util.*

@ReflectedSerializer(ReflectedSerializerBase.ReflectType.MIXED_INJECTION)
class PacketRedirect(private val uuid: UUID, private val packetId: Long) : PacketBase {
    lateinit var packetContainer: ByteArray
        private set

    constructor(uuid: UUID, packetId: Long, packetBase: PacketBase) : this(uuid, packetId) {
        ByteBufAllocator.DEFAULT.heapBuffer().apply {
            writeString(packetBase::class.java.name)
            packetContainer = ByteArray(readableBytes()).apply {
                readBytes(this)
            }
            release()
        }
    }

    constructor(uuid: UUID, packetId: Long, packet: ByteArray) : this(uuid, packetId) {
        packetContainer = packet
    }

    fun asOriginalPacket(): PacketBase {
        return useByteBuf { buf ->
            buf.writeBytes(packetContainer)
            buf.deserializeAs()
        }
    }
}