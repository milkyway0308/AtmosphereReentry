package skywolf46.atmospherereentry.api.packetbridge

import skywolf46.atmospherereentry.api.packetbridge.data.PacketWrapper
import java.util.*

interface PacketBridgeClientConnection {
    fun send(vararg packetBase: PacketBase) {
        send(*packetBase.map { if (it is PacketWrapper<*>) it else PacketWrapper(it, getIdentify()) }.toTypedArray())
    }

    fun send(vararg wrapper: PacketWrapper<*>)

    fun getIdentify(): UUID
}


