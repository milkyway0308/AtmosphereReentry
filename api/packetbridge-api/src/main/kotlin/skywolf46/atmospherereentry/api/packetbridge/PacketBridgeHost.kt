package skywolf46.atmospherereentry.api.packetbridge

interface PacketBridgeHost : PacketListenable {
    fun broadcast(vararg packetBase: PacketBase)

    fun sendTo(target: PacketBridgeClientConnection, vararg packets: PacketBase)
}