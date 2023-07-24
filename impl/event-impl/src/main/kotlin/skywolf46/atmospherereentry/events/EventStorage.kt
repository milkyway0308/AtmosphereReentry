package skywolf46.atmospherereentry.events

import org.koin.core.component.KoinComponent
import skywolf46.atmospherereentry.common.api.UnregisterTrigger

class EventStorage : KoinComponent {
    private val interceptors = sortedMapOf<Int, MutableList<(Any) -> Boolean>>(
        Comparator.comparingInt { it }
    )

    private val interceptorsWithKey = mutableMapOf<String, EventStorage>()

    fun <T : Any> registerInterceptor(priority: Int, listener: (T) -> Boolean): UnregisterTrigger {
        interceptors.getOrPut(priority) { mutableListOf() }.add(listener as (Any) -> Boolean)
        return object : UnregisterTrigger {
            override fun invoke() {
                interceptors[priority]?.remove(listener)
            }
        }
    }

    fun <T : Any> registerObserver(priority: Int, observer: (T) -> Unit): UnregisterTrigger {
        return registerInterceptor<T>(priority) {
            observer(it)
            false
        }
    }

    fun <T : Any> registerInterceptorWithKey(
        key: String,
        priority: Int,
        interceptor: (T) -> Boolean
    ): UnregisterTrigger {
        return interceptorsWithKey.getOrPut(key) { EventStorage() }.registerInterceptor(priority, interceptor)
    }

    fun <T : Any> registerObserverWithKey(key: String, priority: Int, observer: (T) -> Unit): UnregisterTrigger {
        return interceptorsWithKey.getOrPut(key) { EventStorage() }.registerObserver(priority, observer)
    }

    fun <T : Any> callEvent(any: T): T {
        for ((_, v) in interceptors) {
            v.forEach {
                runCatching {
                    if (it.invoke(any))
                        return any
                }.onFailure { it.printStackTrace() }
            }
        }
        return any
    }

    fun <T : Any> callEvent(any: T, key: String): T {
        return interceptorsWithKey[key]?.callEvent(any) ?: any
    }

}