package skywolf46.atmospherereentry.api.packetbridge.data

import org.koin.core.component.KoinComponent
import skywolf46.atmospherereentry.api.packetbridge.PacketBase
import skywolf46.atmospherereentry.api.packetbridge.annotations.ReflectedSerializer
import java.util.*

data class PacketWrapper<T : PacketBase>(
    val packet: T,
    val from: UUID,
    val packetId: Long = -1L
) : KoinComponent, PacketBase {
    internal lateinit var replier: (PacketWrapper<*>, PacketBase) -> Unit

    fun updateReplier(replier: (PacketWrapper<*>, PacketBase) -> Unit) {
        this.replier = replier
    }

    fun reply(packet: PacketBase) {
        replier(this, packet)
    }
}