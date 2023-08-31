package skywolf46.atmospherereentry.common.api.annotations

import kotlin.reflect.KClass

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
annotation class IgnoreException(val type: Array<KClass<out Throwable>> = [])