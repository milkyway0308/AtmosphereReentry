package skywolf46.atmospherereentry.api.packetbridge.annotations

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class PacketListener(val priority: Int = 0)