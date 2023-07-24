package skywolf46.atmospherereentry.api.packetbridge

import org.koin.core.parameter.parametersOf
import org.koin.mp.KoinPlatform
import skywolf46.atmospherereentry.api.packetbridge.data.ListenerType

interface PacketBridgeHost : PacketListenable {
    companion object {
        fun createInstance(
            port: Int,
            skipVerification: Boolean,
            listenerType: ListenerType = ListenerType.Reflective.asServer()
        ): PacketBridgeHost {
            return KoinPlatform.getKoin().get { parametersOf(port, skipVerification, listenerType) }
        }
    }

    fun broadcast(vararg packetBase: PacketBase)

    fun sendTo(target: PacketBridgeClientConnection, vararg packets: PacketBase)
}