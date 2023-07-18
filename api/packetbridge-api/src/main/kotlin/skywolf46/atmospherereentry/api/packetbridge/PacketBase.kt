package skywolf46.atmospherereentry.api.packetbridge

import org.koin.core.component.KoinComponent
import org.koin.core.component.get

interface PacketBase : KoinComponent {
    fun requireMainThreadProcess(): Boolean {
        return false
    }

    fun getPacketProcessGroup(): String {
        return "Default"
    }
}