package skywolf46.atmospherereentry.packetbridge.util

internal fun log(msg: String) {
    println("[PacketBridge] $msg")
}

internal fun logError(msg: String) {
    System.err.println("[PacketBridge] $msg")
    System.err.flush()
    Thread.sleep(5L)
}