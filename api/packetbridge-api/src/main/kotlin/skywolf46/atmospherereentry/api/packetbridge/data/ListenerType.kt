package skywolf46.atmospherereentry.api.packetbridge.data

import skywolf46.atmospherereentry.api.packetbridge.PacketListenable
import skywolf46.atmospherereentry.api.packetbridge.annotations.PacketListener
import skywolf46.atmospherereentry.api.packetbridge.annotations.ServerPacketListener
import skywolf46.atmospherereentry.events.api.EventReflectionFilter
import kotlin.reflect.KClass

sealed interface ListenerType {
    fun onRegister(listenable: PacketListenable)

    object Direct : ListenerType {
        override fun onRegister(listenable: PacketListenable) {
            // Do nothing
        }
    }

    class Reflective<T : Annotation>(
        private val classAnnotation: Class<out Annotation>,
        private val functionAnnotation: Class<out T>,
        private val priorityProvider: (T) -> Int
    ) : ListenerType {
        companion object {
            fun asClient(): ListenerType {
                return Reflective(PacketListener::class.java, PacketListener::class.java) { it.priority }
            }

            fun asServer(): ListenerType {
                return Reflective(ServerPacketListener::class.java, ServerPacketListener::class.java) { it.priority }
            }
        }

        @OptIn(skywolf46.atmospherereentry.common.annotations.Reflective::class)
        override fun onRegister(listenable: PacketListenable) {
            listenable.getPacketListener().scanAndRegisterWithFilter(
                classAnnotation,
                functionAnnotation,
                priorityProvider,
                EventReflectionFilter(PacketWrapper::class.java) { cls, field, invoker ->
                    val targetClass =
                        cls.kotlin.supertypes.find { it.classifier is KClass<*> && it.classifier == PacketWrapper::class }!!.arguments[0].type!!.classifier!! as KClass<*>
                    registerObserverWithKey<PacketWrapper<*>>(
                        PacketWrapper::class,
                        targetClass.qualifiedName!!,
                        priorityProvider(field.getAnnotation(functionAnnotation))
                    ) {
                        invoker(it)
                    }
                }
            )
        }
    }
}