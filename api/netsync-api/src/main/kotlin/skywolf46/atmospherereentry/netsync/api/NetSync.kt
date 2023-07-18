package skywolf46.atmospherereentry.netsync.api

import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import kotlin.reflect.KClass

abstract class NetSync(val strategy: NetSyncStrategy = NetSyncStrategy.LAZY) : KoinComponent {
    protected fun <T : Any> sync(
        data: KClass<T>,
        fieldName: String,
        strategy: NetSyncStrategy = this.strategy
    ): Lazy<Updatable<T>> {
        return lazy(LazyThreadSafetyMode.SYNCHRONIZED) { get<NetSyncEndPoint>().ready(data, fieldName, strategy) }
    }

    protected fun <T : Any> sync(
        data: KClass<T>,
        strategy: NetSyncStrategy = this.strategy
    ): Lazy<Updatable<T>> {
        return lazy(LazyThreadSafetyMode.SYNCHRONIZED) { get<NetSyncEndPoint>().ready(data, strategy) }
    }

    protected inline fun <reified T : Any> sync(strategy: NetSyncStrategy = this.strategy): Lazy<Updatable<T>> {
        return sync(T::class, strategy)
    }

    protected inline fun <reified T : Any> sync(
        fieldName: String,
        strategy: NetSyncStrategy = this.strategy
    ): Lazy<Updatable<T>> {
        return sync(T::class, fieldName, strategy)
    }

    protected suspend inline fun transaction(vararg lock: Updatable<out Any>, unit: () -> Unit) {
        lock.forEach { it.getAndLock() }
        unit()
        lock.forEach { it.unlock() }
    }

    protected suspend inline fun occupy(vararg lock: Updatable<out Any>, unit: () -> Unit) {
        get<NetSyncEndPoint>().withNetworkLock(*lock, unit = unit)
    }
}