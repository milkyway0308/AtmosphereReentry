package skywolf46.atmospherereentry.events.api

import skywolf46.atmospherereentry.common.UnregisterTrigger
import skywolf46.atmospherereentry.common.annotations.Reflective
import kotlin.reflect.KClass

interface EventManager {
    fun createEventManager(): EventManager

    fun <T : Any> registerInterceptor(
        target: KClass<out Any>,
        priority: Int,
        listener: (T) -> Boolean
    ): UnregisterTrigger

    fun <T : Any> registerInterceptorWithKey(
        target: KClass<out Any>,
        key: String,
        priority: Int,
        listener: (T) -> Boolean
    ): UnregisterTrigger

    fun <T : Any> registerObserver(target: KClass<out Any>, priority: Int, observer: (T) -> Unit): UnregisterTrigger

    fun <T : Any> registerObserverWithKey(
        target: KClass<out Any>,
        key: String,
        priority: Int,
        observer: (T) -> Unit
    ): UnregisterTrigger

    @Reflective
    fun <CLS : Annotation, FUNCT : Annotation> scanAndRegisterWithFilter(
        classAnnotation: Array<Class<CLS>>,
        functionAnnotation: Class<FUNCT>,
        priorityProvider: (FUNCT) -> Int,
        vararg filter: EventReflectionFilter<*>
    )


    @Reflective
    fun <CLS : Annotation, FUNCT : Annotation> scanAndRegisterWithFilter(
        classAnnotation: Class<CLS>,
        functionAnnotation: Class<FUNCT>,
        priorityProvider: (FUNCT) -> Int,
        vararg filter: EventReflectionFilter<*>
    ) {
        scanAndRegisterWithFilter(arrayOf(classAnnotation), functionAnnotation, priorityProvider, *filter)
    }

    @Reflective
    fun <CLS : Annotation, FUNCT : Annotation> scanAndRegister(
        classAnnotation: Class<CLS>,
        functionAnnotation: Class<FUNCT>,
        priorityProvider: (FUNCT) -> Int,
    ) {
        scanAndRegisterWithFilter(arrayOf(classAnnotation), functionAnnotation, priorityProvider, *emptyArray())
    }



    fun callEvent(any: Any): Any

    fun callEvent(any: Any, key: String): Any

    fun clear()
}