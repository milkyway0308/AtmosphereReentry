package skywolf46.atmospherereentry.packetbridge

import io.netty.channel.Channel
import skywolf46.atmospherereentry.api.packetbridge.PacketBridgeClientConnection
import skywolf46.atmospherereentry.api.packetbridge.data.PacketWrapper
import java.util.*

class PacketBridgeProxy(val uuid: UUID, private val channel: Channel) : PacketBridgeClientConnection {
    var identifiedId: String? = null
        internal set

    override fun send(vararg wrapper: PacketWrapper<*>) {
        wrapper.forEach {
            runCatching { channel.write(it) }.onFailure { e -> e.printStackTrace() }
        }
        channel.flush()
    }

    override fun getIdentify(): UUID {
        return uuid
    }

    fun isIdentified() = identifiedId != null
}