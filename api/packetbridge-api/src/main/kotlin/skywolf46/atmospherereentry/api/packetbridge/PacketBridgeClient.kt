package skywolf46.atmospherereentry.api.packetbridge

import org.koin.core.parameter.parametersOf
import org.koin.mp.KoinPlatform
import skywolf46.atmospherereentry.api.packetbridge.data.ListenerType
import java.util.concurrent.Future
import kotlin.reflect.KClass

interface PacketBridgeClient : PacketListenable, PacketBridgeClientConnection {

    companion object {
        fun createInstance(
            host: String,
            port: Int,
            identifyKey: String,
            listenerType: ListenerType = ListenerType.Reflective.asClient(),
        ): PacketBridgeClient {
            return KoinPlatform.getKoin()
                .get<PacketBridgeClient>(parameters = { parametersOf(host, port, identifyKey, listenerType) })
        }
    }

    fun <T : PacketBase> waitReply(
        packetBase: PacketBase,
        limitation: KClass<T>,
        listener: (T) -> Unit
    )

    suspend fun <T : PacketBase> waitReply(packetBase: PacketBase, limitation: KClass<T>): T

    fun <T : PacketBase> waitReplyAsync(packetBase: PacketBase, limitation: KClass<T>): Future<T>

    fun broadcast(vararg packetBase: PacketBase)

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