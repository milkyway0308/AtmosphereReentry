package skywolf46.atmospherereentry.packetbridge.util

import skywolf46.atmospherereentry.api.packetbridge.PacketBase
import java.util.concurrent.CompletableFuture

class TriggerableFuture<T> : CompletableFuture<T>() {
    fun trigger(packet: T) {
        complete(packet)
    }
}