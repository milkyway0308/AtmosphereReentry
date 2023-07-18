package skywolf46.atmospherereentry.api.packetbridge

import java.util.concurrent.Future
import kotlin.reflect.KClass

interface PacketBridgeClient : PacketListenable, PacketBridgeClientConnection {

    fun <T : PacketBase> waitReply(
        packetBase: PacketBase,
        limitation: KClass<T>,
        listener: (T) -> Unit
    )

    suspend fun <T : PacketBase> waitReply(packetBase: PacketBase, limitation: KClass<T>): T

    fun <T : PacketBase> waitReplyAsync(packetBase: PacketBase, limitation: KClass<T>): Future<T>

    fun getInfo(): PacketBridgeClientConnection
}

inline fun <reified T : PacketBase> PacketBridgeClient.waitReplyOf(
    packetBase: PacketBase,
    noinline listener: (T) -> Unit
) {
    waitReply(packetBase, T::class, listener)
}

suspend inline fun <reified T : PacketBase> PacketBridgeClient.waitReplyOf(packetBase: PacketBase): T {
    return waitReply(packetBase, T::class)
}

inline fun <reified T : PacketBase> PacketBridgeClient.waitReplyAsyncOf(packetBase: PacketBase): Future<T> {
    return waitReplyAsync(packetBase, T::class)
}