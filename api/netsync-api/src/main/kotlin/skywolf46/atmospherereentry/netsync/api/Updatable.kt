package skywolf46.atmospherereentry.netsync.api

import arrow.core.Option
import org.koin.core.component.KoinComponent
import skywolf46.atmospherereentry.api.packetbridge.data.DoubleHashedType
import java.util.concurrent.Future

interface Updatable<T: Any> : KoinComponent {

    fun getFuture() : Future<Option<T>>

    suspend fun getOption() : Option<T>

    @Throws(NullPointerException::class)
    suspend fun get() : T

    suspend fun set(data: Option<T>)

    suspend fun getAndLock() : Option<T>

    suspend fun unlock()

    suspend fun getAndUpdate(unit: (Option<T>) -> Option<T>)

    suspend fun updateAndGet(value: Option<T>): Option<T>

    fun getIdentify() : DoubleHashedType
}