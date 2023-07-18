package skywolf46.atmospherereentry.netsync

import arrow.core.Option
import arrow.core.toOption
import skywolf46.atmospherereentry.netsync.api.NetSyncEndPoint
import skywolf46.atmospherereentry.netsync.api.NetSyncStrategy
import skywolf46.atmospherereentry.netsync.api.NetworkLock
import skywolf46.atmospherereentry.netsync.api.Updatable
import skywolf46.atmospherereentry.netsync.packets.NetworkLockImpl
import skywolf46.atmospherereentry.netsync.updatable.LazyUpdatableImpl
import skywolf46.atmospherereentry.netsync.updatable.SharedUpdatableImpl
import kotlin.reflect.KClass

class NetSyncEndPointImpl : NetSyncEndPoint {
    override suspend fun <T : Any> acquire(type: KClass<T>, strategy: NetSyncStrategy): Updatable<T> {
        return when (strategy) {
            NetSyncStrategy.LAZY -> {
                LazyUpdatableImpl(type.java, Option.fromNullable(null))
            }

            NetSyncStrategy.SHARED -> {
                SharedUpdatableImpl()
            }
        }
    }

    override suspend fun <T : Any> acquire(
        type: KClass<T>,
        fieldName: String,
        strategy: NetSyncStrategy
    ): Updatable<T> {
        return when (strategy) {
            NetSyncStrategy.LAZY -> {
                LazyUpdatableImpl(type.java, fieldName.toOption()).apply {
                    get()
                }
            }

            NetSyncStrategy.SHARED -> {
                SharedUpdatableImpl()
            }
        }
    }

    override fun <T : Any> ready(type: KClass<T>, strategy: NetSyncStrategy): Updatable<T> {
        return when (strategy) {
            NetSyncStrategy.LAZY -> {
                LazyUpdatableImpl(type.java, Option.fromNullable(null))
            }

            NetSyncStrategy.SHARED -> {
                SharedUpdatableImpl()
            }
        }
    }

    override fun <T : Any> ready(type: KClass<T>, fieldName: String, strategy: NetSyncStrategy): Updatable<T> {
        return when (strategy) {
            NetSyncStrategy.LAZY -> {
                LazyUpdatableImpl(type.java, fieldName.toOption())
            }

            NetSyncStrategy.SHARED -> {
                SharedUpdatableImpl()
            }
        }
    }

    override fun lockNetwork(vararg updatable: Updatable<out Any>): NetworkLock {
        TODO()
    }
}