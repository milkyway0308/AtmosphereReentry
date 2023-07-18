package skywolf46.atmospherereentry.netsync.updatable

import arrow.core.Option
import arrow.core.getOrElse
import arrow.core.toOption
import org.koin.core.component.inject
import skywolf46.atmospherereentry.api.packetbridge.PacketBridgeClient
import skywolf46.atmospherereentry.api.packetbridge.data.DoubleHashedType
import skywolf46.atmospherereentry.api.packetbridge.packets.GenericPacketResult
import skywolf46.atmospherereentry.api.packetbridge.waitReplyAsyncOf
import skywolf46.atmospherereentry.api.packetbridge.waitReplyOf
import skywolf46.atmospherereentry.netsync.api.Updatable
import skywolf46.atmospherereentry.netsync.packets.request.RequestVariableSync
import skywolf46.atmospherereentry.netsync.packets.request.RequestVariableUpdate
import skywolf46.atmospherereentry.netsync.packets.request.RequestVariableUpdateAndSync
import skywolf46.atmospherereentry.netsync.packets.response.ResponseVariableSync
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicBoolean

class LazyUpdatableImpl<T : Any>(cls: Class<T>, private val fieldName: Option<String>) : Updatable<T> {
    private val sharedUpdatableExecutor = Executors.newCachedThreadPool()

    private val classHash = DoubleHashedType(cls)

    private val client by inject<PacketBridgeClient>()

    private var lastValue: Option<T> = Option.fromNullable(null)

    private val lock = AtomicBoolean(false)

    private val modified = AtomicBoolean(false)

    override fun getFuture(): Future<Option<T>> {
        return sharedUpdatableExecutor.submit<Option<T>> {
            client.waitReplyAsyncOf<ResponseVariableSync<T>>(RequestVariableSync(classHash, fieldName)).get().data
        }
    }

    override suspend fun getOption(): Option<T> {
        return client.waitReplyOf<ResponseVariableSync<T>>(RequestVariableSync(classHash, fieldName)).data
    }

    override suspend fun get(): T {
        return (if (lock.get())
            lastValue.getOrElse { throw NullPointerException() }
        else getOption().orNull()?.apply {
            lastValue = this.toOption()
        }) ?: throw NullPointerException()
    }

    override suspend fun getAndLock(): Option<T> {
        if (lock.get()) {
            return lastValue
        }
        get()
        lock.set(true)
        return lastValue
    }

    override suspend fun unlock() {
        if (!lock.get()) {
            return
        }
        lock.set(false)
        if (modified.getAndSet(false)) {
            client.waitReplyOf<GenericPacketResult>(RequestVariableUpdate(classHash, fieldName, lastValue.toOption()))
        }
    }

    override fun getIdentify(): DoubleHashedType {
        TODO("Not yet implemented")
    }

    override suspend fun updateAndGet(value: Option<T>): Option<T> {
        return client.waitReplyOf<RequestVariableUpdateAndSync<T>>(
            RequestVariableUpdate(
                classHash,
                fieldName,
                value
            )
        ).data
    }

    override suspend fun getAndUpdate(unit: (Option<T>) -> Option<T>) {
        getOption().apply {
            client.waitReplyAsyncOf<GenericPacketResult>(RequestVariableUpdate(classHash, fieldName, unit(this)))
        }
    }

    override suspend fun set(data: Option<T>) {
        client.waitReplyOf<GenericPacketResult>(RequestVariableUpdate(classHash, fieldName, data))
    }

}