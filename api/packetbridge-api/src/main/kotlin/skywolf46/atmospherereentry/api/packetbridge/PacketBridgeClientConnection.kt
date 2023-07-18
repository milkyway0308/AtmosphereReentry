package skywolf46.atmospherereentry.api.packetbridge

import skywolf46.atmospherereentry.api.packetbridge.data.PacketWrapper
import java.util.UUID

interface PacketBridgeClientConnection {
    fun send(vararg packetBase: PacketBase) {
        send(*packetBase.map { PacketWrapper(it, getIdentify()) }.toTypedArray())
    }

    fun send(vararg wrapper: PacketWrapper<*>)

    fun getIdentify() : UUID
}


