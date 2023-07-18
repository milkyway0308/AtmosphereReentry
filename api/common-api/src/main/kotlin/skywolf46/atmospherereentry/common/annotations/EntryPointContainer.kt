package skywolf46.atmospherereentry.common.annotations

import kotlin.reflect.KClass

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class EntryPointContainer(vararg val dependsOn: KClass<*>)