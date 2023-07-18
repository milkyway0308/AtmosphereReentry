package skywolf46.atmospherereentry.netsync.api

import kotlin.reflect.KClass

interface NetSyncEndPoint {
    suspend fun <T : Any> acquire(type: KClass<T>, strategy: NetSyncStrategy): Updatable<T>

    suspend fun <T : Any> acquire(type: KClass<T>, fieldName: String, strategy: NetSyncStrategy): Updatable<T>

    fun <T : Any> ready(type: KClass<T>, strategy: NetSyncStrategy): Updatable<T>

    fun <T : Any> ready(type: KClass<T>, fieldName: String, strategy: NetSyncStrategy): Updatable<T>

    fun lockNetwork(vararg updatable: Updatable<out Any>): NetworkLock
}

suspend inline fun <reified T : Any> NetSyncEndPoint.acquire(strategy: NetSyncStrategy = NetSyncStrategy.LAZY): Updatable<T> {
    return acquire(T::class, strategy)
}

suspend inline fun <reified T : Any> NetSyncEndPoint.acquire(
    fieldName: String,
    strategy: NetSyncStrategy = NetSyncStrategy.LAZY
): Updatable<T> {
    return acquire(T::class, fieldName, strategy)
}

suspend inline fun NetSyncEndPoint.withNetworkLock(
    vararg updatable: Updatable<out Any>,
    unit: () -> Unit
) {
    lockNetwork(*updatable).apply {
        this.acquire()
        unit()
        this.release()
    }
}


suspend inline fun NetSyncEndPoint.withImmediateNetworkLock(
    vararg updatable: Updatable<out Any>,
    unit: () -> Unit
): Boolean {
    return lockNetwork(*updatable).run {
        if (this.acquireOrFail()) {
            return@run false
        }
        unit()
        this.release()
        return@run true
    }
}