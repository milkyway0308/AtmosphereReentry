package skywolf46.atmospherereentry.api.packetbridge

import skywolf46.atmospherereentry.common.api.UnregisterTrigger
import skywolf46.atmospherereentry.events.api.EventManager

interface PacketListenable {
    fun <T : PacketBase> addListener(type: Class<T>, priority: Int, listener: (T) -> Unit): UnregisterTrigger

    fun getPacketListener() : EventManager

    fun trigger(packetBase: PacketBase)
}

inline fun <reified T : PacketBase> PacketListenable.addListener(noinline listener: (T) -> Unit) =
    addListener(T::class.java, 0, listener)