package skywolf46.atmospherereentry.common.api.annotations

import kotlin.reflect.KClass

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class EntryPointContainer(vararg val dependsOn: KClass<*>)