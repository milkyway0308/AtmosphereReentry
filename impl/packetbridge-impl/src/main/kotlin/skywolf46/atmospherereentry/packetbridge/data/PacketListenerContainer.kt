package skywolf46.atmospherereentry.packetbridge.data

import skywolf46.atmospherereentry.api.packetbridge.PacketContainer

class PacketListenerContainer {
    private val listeners = mutableMapOf<Class<*>, (PacketContainer<*>) -> Unit>()

    fun registerListeners(cls: Class<*>, listener: (PacketContainer<*>) -> Unit) {
        listeners[cls] = listener
    }

    fun triggerListener() {

    }
}