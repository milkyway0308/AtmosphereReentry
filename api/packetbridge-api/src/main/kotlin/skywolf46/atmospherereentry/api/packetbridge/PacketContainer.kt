package skywolf46.atmospherereentry.api.packetbridge

interface PacketContainer<T : PacketBase> {
    fun get(): T

    fun reply(packet: T): Boolean

    fun <RECEIVE : PacketBase> replyAndWait(packet: T, listener: (RECEIVE) -> Unit): T
}