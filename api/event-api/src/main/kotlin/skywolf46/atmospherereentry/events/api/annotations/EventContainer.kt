package skywolf46.atmospherereentry.events.api.annotations

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class EventContainer(val priority: Int = 0)