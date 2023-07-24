package skywolf46.atmospherereentry.netsync

import arrow.core.Option
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import skywolf46.atmospherereentry.api.packetbridge.PacketBridgeClient
import skywolf46.atmospherereentry.api.packetbridge.addListener
import skywolf46.atmospherereentry.api.packetbridge.data.DoubleHashedType
import skywolf46.atmospherereentry.common.api.UnregisterTrigger
import skywolf46.atmospherereentry.netsync.packets.broadcast.BroadcastVariableSync
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write
import kotlin.reflect.KClass


class NetSyncPacketListenerRegistry() : KoinComponent {
    private val client = get<PacketBridgeClient>()
    private val listeners = mutableMapOf<Pair<DoubleHashedType, String?>, SynchronizedListenerRegistry>()
    private val registryGlobalLock = ReentrantReadWriteLock()

    init {
        client.addListener<BroadcastVariableSync> { packet ->
            listeners[packet.doubleBuffer to packet.fieldName.orNull()]?.invokeListener(packet.data)
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> registerListener(
        type: KClass<*>,
        fieldName: Option<String> = Option.fromNullable(null),
        listener: (Option<T>) -> Unit
    ) : UnregisterTrigger {
        return registryGlobalLock.write {
            listeners.getOrPut(DoubleHashedType(type) to fieldName.orNull()) { SynchronizedListenerRegistry() }
                .addListener(listener as (Option<Any>) -> Unit)
        }
    }

    class SynchronizedListenerRegistry {
        private val listeners = mutableListOf<(Option<Any>) -> Unit>()
        private val lock = ReentrantReadWriteLock()

        fun addListener(unit: (Option<Any>) -> Unit): UnregisterTrigger {
            lock.write {
                listeners.add(unit)
            }
            return object : UnregisterTrigger {
                override fun invoke() {
                    lock.write {
                        listeners.remove(unit)
                    }
                }
            }
        }

        fun invokeListener(data: Option<Any>) {
            lock.read {
                listeners.forEach { it.invoke(data) }
            }
        }
    }
}