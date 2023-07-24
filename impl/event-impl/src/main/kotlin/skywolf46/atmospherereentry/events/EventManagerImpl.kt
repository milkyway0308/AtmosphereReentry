package skywolf46.atmospherereentry.events

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
import io.github.classgraph.ClassInfoList
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import skywolf46.atmospherereentry.common.api.UnregisterTrigger
import skywolf46.atmospherereentry.common.api.annotations.Reflective
import skywolf46.atmospherereentry.common.api.util.printError
import skywolf46.atmospherereentry.events.api.EventManager
import skywolf46.atmospherereentry.events.api.EventReflectionFilter
import kotlin.reflect.KClass

class EventManagerImpl : EventManager, KoinComponent {
    private val eventStorages = mutableMapOf<Class<out Any>, EventStorage>()

    override fun createEventManager(): EventManager {
        return EventManagerImpl()
    }

    override fun <T : Any> registerInterceptor(
        target: KClass<out Any>,
        priority: Int,
        listener: (T) -> Boolean
    ): UnregisterTrigger {
        return eventStorages.getOrPut(target.java) { EventStorage() }.registerInterceptor(priority, listener)
    }

    override fun <T : Any> registerInterceptorWithKey(
        target: KClass<out Any>,
        key: String,
        priority: Int,
        listener: (T) -> Boolean
    ): UnregisterTrigger {
        return eventStorages.getOrPut(target.java) { EventStorage() }
            .registerInterceptorWithKey(key, priority, listener)
    }

    override fun <T : Any> registerObserver(
        target: KClass<out Any>,
        priority: Int,
        observer: (T) -> Unit
    ): UnregisterTrigger {
        return eventStorages.getOrPut(target.java) { EventStorage() }.registerObserver(priority, observer)
    }

    override fun <T : Any> registerObserverWithKey(
        target: KClass<out Any>,
        key: String,
        priority: Int,
        observer: (T) -> Unit
    ): UnregisterTrigger {
        return eventStorages.getOrPut(target.java) { EventStorage() }.registerObserverWithKey(key, priority, observer)
    }

    override fun callEvent(any: Any): Any {
        return eventStorages[any.javaClass]?.callEvent(any) ?: any.right()
    }

    override fun callEvent(any: Any, key: String): Any {
        return eventStorages[any.javaClass]?.callEvent(any, key) ?: any.right()
    }

    @Reflective
    override fun <CLS : Annotation, FUNCT : Annotation> scanAndRegisterWithFilter(
        classAnnotation: Array<Class<CLS>>,
        functionAnnotation: Class<FUNCT>,
        priorityProvider: (FUNCT) -> Int,
        vararg filter: EventReflectionFilter<out Any>
    ) {
        val classFilter = filter.associateBy { it.targetClass }.mapValues { it.value as EventReflectionFilter<Any> }
        get<ClassInfoList>().filter { classInfo -> classAnnotation.any { classInfo.hasAnnotation(it) } }
            .forEach { classInfo ->
                runCatching {
                    val instance = acquireInstance(classInfo.loadClass()).getOrElse {
                        printError("Failed to acquire instance for ${classInfo.name}")
                    }
                    instance.javaClass.declaredMethods.filter { it.getAnnotation(functionAnnotation) != null }
                        .forEach { method ->
                            runCatching {
                                if (method.parameterCount != 1)
                                    throw IllegalArgumentException("Event listener must have only one parameter")
                                val priority = priorityProvider(method.getAnnotation(functionAnnotation)!!)
                                method.isAccessible = true
                                if (classFilter.containsKey(method.parameters[0].type.kotlin.java)) {
                                    classFilter[method.parameters[0].type.kotlin.java]!!.customRegistrar(
                                        this@EventManagerImpl,
                                        method.parameters[0].type.kotlin.java as Class<Any>,
                                        method
                                    ) {
                                        method.invoke(instance, it)
                                    }
                                } else {
                                    registerObserver<Any>(method.parameters[0].type.kotlin, priority) { data ->
                                        method.invoke(instance, data)
                                    }
                                }
                            }.onFailure {
                                printError("Failed to register event listener for ${classInfo.name}#${method.name} : ${it.javaClass.name} (${it.message})")
                                it.printStackTrace()
                            }
                        }
                }
            }
    }

    private fun acquireInstance(cls: Class<*>): Either<Throwable, Any> {
        if (cls.kotlin.objectInstance != null)
            return cls.kotlin.objectInstance!!.right()
        return cls.constructors.find { it.parameterCount == 0 }?.run {
            isAccessible = true
            runCatching {
                newInstance()
            }.getOrElse {
                return it.left()
            }
        }?.right() ?: NoSuchMethodException("No empty constructor found for ${cls.name}").left()
    }

    override fun clear() {
        eventStorages.clear()
    }
}