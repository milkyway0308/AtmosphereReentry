package skywolf46.atmospherereentry.packetbridge.data

import skywolf46.atmospherereentry.api.packetbridge.PacketBase
import java.util.concurrent.TimeUnit


class ReplyMetadata(
    val expireOn: Long = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(2),
    val action: (PacketBase) -> Unit
) {
    fun isExpired(): Boolean {
        return System.currentTimeMillis() > expireOn
    }
}