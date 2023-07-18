package skywolf46.atmospherereentry.packetbridge.packets

import io.netty.buffer.ByteBufAllocator
import org.koin.core.component.KoinComponent
import skywolf46.atmospherereentry.api.packetbridge.PacketBase
import skywolf46.atmospherereentry.api.packetbridge.util.deserializeAs
import skywolf46.atmospherereentry.api.packetbridge.util.useByteBuf
import skywolf46.atmospherereentry.api.packetbridge.util.writeString

class WrappedPacket(val packetId: Long) : PacketBase, KoinComponent {
    lateinit var packetContainer: ByteArray
        private set

    constructor(packetId: Long, packetBase: PacketBase) : this(packetId) {
        ByteBufAllocator.DEFAULT.heapBuffer().apply {
            writeString(packetBase::class.java.name)
            packetContainer = ByteArray(readableBytes()).apply {
                readBytes(this)
            }
            release()
        }
    }

    fun asOriginalPacket(): PacketBase {
        return useByteBuf { buf ->
            buf.writeBytes(packetContainer)
            buf.deserializeAs()
        }
    }


}