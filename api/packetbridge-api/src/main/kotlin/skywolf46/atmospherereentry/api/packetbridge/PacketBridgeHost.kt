package skywolf46.atmospherereentry.api.packetbridge

import org.koin.core.parameter.parametersOf
import org.koin.mp.KoinPlatform
import skywolf46.atmospherereentry.api.packetbridge.data.ListenerType
import skywolf46.atmospherereentry.api.packetbridge.util.JwtProvider

interface PacketBridgeHost : PacketListenable {
    companion object {
        fun createInstance(
            port: Int,
            jwtProvider: JwtProvider,
            listenerType: ListenerType = ListenerType.Reflective.asServer()
        ): PacketBridgeHost {
            return KoinPlatform.getKoin().get { parametersOf(port, jwtProvider, listenerType) }
        }
    }

    fun getProvider(): JwtProvider?

    fun broadcast(vararg packetBase: PacketBase)

    fun sendTo(target: PacketBridgeClientConnection, vararg packets: PacketBase)
}