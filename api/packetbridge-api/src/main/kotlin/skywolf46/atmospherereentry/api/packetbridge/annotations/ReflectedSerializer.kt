package skywolf46.atmospherereentry.api.packetbridge.annotations

import skywolf46.atmospherereentry.api.packetbridge.ReflectedSerializerBase

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class ReflectedSerializer(val type: ReflectedSerializerBase.ReflectType = ReflectedSerializerBase.ReflectType.CONSTRUCTOR_INJECTION)