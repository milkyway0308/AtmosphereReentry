package skywolf46.atmospherereentry.netsync.updatable

import arrow.core.Option
import skywolf46.atmospherereentry.api.packetbridge.data.DoubleHashedType
import skywolf46.atmospherereentry.netsync.api.Updatable
import java.io.Closeable
import java.util.concurrent.Future

class SharedUpdatableImpl<T : Any> : Updatable<T>, Closeable {


    override fun getFuture(): Future<Option<T>> {
        TODO("Not yet implemented")
    }

    override suspend fun getOption(): Option<T> {
        TODO("Not yet implemented")
    }

    override suspend fun get(): T {
        TODO("Not yet implemented")
    }

    override suspend fun getAndLock(): Option<T> {
        TODO("Not yet implemented")
    }

    override suspend fun unlock() {
        TODO("Not yet implemented")
    }

    override fun getIdentify(): DoubleHashedType {
        TODO("Not yet implemented")
    }

    override suspend fun updateAndGet(value: Option<T>): Option<T> {
        TODO("Not yet implemented")
    }

    override suspend fun getAndUpdate(unit: (Option<T>) -> Option<T>) {
        TODO("Not yet implemented")
    }

    override suspend fun set(data: Option<T>) {
        TODO("Not yet implemented")
    }

    override fun close() {

    }
}