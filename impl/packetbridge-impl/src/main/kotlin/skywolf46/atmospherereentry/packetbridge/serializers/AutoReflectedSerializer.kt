package skywolf46.atmospherereentry.packetbridge.serializers

import arrow.core.getOrElse
import io.netty.buffer.ByteBuf
import org.koin.core.component.get
import skywolf46.atmospherereentry.api.packetbridge.DataSerializerBase
import skywolf46.atmospherereentry.api.packetbridge.DataSerializerRegistry
import skywolf46.atmospherereentry.api.packetbridge.ReflectedSerializerBase
import skywolf46.atmospherereentry.api.packetbridge.annotations.Exclude
import skywolf46.atmospherereentry.api.packetbridge.data.DoubleHashedType
import skywolf46.atmospherereentry.api.packetbridge.util.deserializeAs
import skywolf46.atmospherereentry.api.packetbridge.util.serializeTo
import java.lang.reflect.Array
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.util.*
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.javaConstructor
import kotlin.reflect.jvm.kotlinFunction

class AutoReflectedSerializer<T : Any>(target: Class<T>, type: ReflectType) : ReflectedSerializerBase<T>(target, type) {
    private val serializer = ConstructedSerializer(target, emptyList(), type)

    override fun serialize(buf: ByteBuf, dataBase: T) {
        serializer.serialize(buf, dataBase)
    }

    override fun deserialize(buf: ByteBuf): T {
        return serializer.deserialize(buf)
    }

    private class ConstructedSerializer<T : Any>(
        private val target: Class<T>,
        private val constructedClass: List<Class<*>>,
        private val type: ReflectType
    ) : DataSerializerBase<T>() {
        private val serializers = mutableListOf<DataSerializerBase<Any>>()
        private val preCalculatedHash: Int
        private val targetConstructor: Constructor<out Any>
        private val fieldMap = mutableMapOf<DoubleHashedType, Field>()

        init {
            verifyReflectType()
            constructSerializer(type)
            preCalculatedHash = calculateHashCode()
            targetConstructor = findConstructor(type)
            target.declaredFields.forEach {
                fieldMap[DoubleHashedType(it.name)] = it
            }
        }

        private fun verifyReflectType() {
            if (type == ReflectType.FIELD_INJECTION) {
                if (target.constructors.any { it.parameterCount != 0 }) {
                    throw IllegalArgumentException("Field injection requires empty constructor. (Class ${target.name})")
                }
            } else {
//                if (target.getAnnotation(Metadata::class.java) == null) {
//                    throw IllegalArgumentException("Constructor / Mixed injection cannot be used in java based class. Use kotlin based class instead. (Class ${target.name})")
//                }
                if (target.kotlin.primaryConstructor == null) {
                    throw IllegalArgumentException("Constructor / Mixed injection cannot be used in class without primary constructor. (Class ${target.name})")
                }
                if (target.kotlin.primaryConstructor!!.parameters.any { parameter -> target.kotlin.declaredMemberProperties.find { it.name == parameter.name } == null }) {
                    throw IllegalArgumentException("Primary constructor of Constructor / Mixed Injection cannot use non-property parameter. (Class ${target.name})")
                }
            }
        }

        private fun findConstructor(type: ReflectType): Constructor<out Any> {
            if (type == ReflectType.FIELD_INJECTION) {
                return target.constructors.first()
            }
            return target.kotlin.primaryConstructor!!.javaConstructor!!
        }

        private fun constructSerializer(type: ReflectType) {
//            if (constructedClass.contains(javaClass)) {
//                throw IllegalStateException("Circular dependency detected")
//            }
            val registry = get<DataSerializerRegistry>()
            when (type) {
                ReflectType.CONSTRUCTOR_INJECTION -> {
                    target.kotlin.primaryConstructor!!.parameters.forEach {
                        val field = target.getDeclaredField(it.name!!)
                        serializers.add(registry.acquireSerializer(field.type).getOrElse {
                            throw IllegalStateException("Parameter ${it.name} has no serializer. (Class ${field.type.name})")
                        })
                    }
                }

                ReflectType.FIELD_INJECTION -> {
                    target.declaredFields.forEach {
                        if (Modifier.isFinal(it.modifiers))
                            return@forEach
                        serializers.add(registry.acquireSerializer(it.type).getOrElse {
                            throw IllegalStateException("Parameter ${it.name} has no serializer. (Class ${it.type.name})")
                        })
                    }
                }

                ReflectType.MIXED_INJECTION -> {
                    val constructorParameters = target.kotlin.primaryConstructor!!.parameters.map { it.name }
                    target.declaredFields.forEach {
                        if (Modifier.isFinal(it.modifiers) && it.name !in constructorParameters)
                            return@forEach
                        if (it.name !in constructorParameters && it.getAnnotation(Exclude::class.java) != null) {
                            return@forEach
                        }
                        serializers.add(registry.acquireSerializer(it.type).getOrElse {
                            throw IllegalStateException("Parameter ${it.name} has no serializer. (Class ${it.type.name})")
                        })
                    }
                }
            }
        }

        override fun serialize(buf: ByteBuf, dataBase: T) {
            buf.writeByte(type.ordinal)
            buf.writeInt(preCalculatedHash)
            dataBase.javaClass.declaredFields.forEachIndexed { i, it ->
                it.isAccessible = true
                DoubleHashedType(it.name).serializeTo(buf)
                it.get(dataBase)?.apply {
                    buf.writeBoolean(false)
                    serializers[i].serialize(buf, this)
                } ?: run {
                    buf.writeBoolean(true)
                }
            }
        }

        override fun deserialize(buf: ByteBuf): T {
            if (buf.readByte().toInt() != type.ordinal)
                throw IllegalArgumentException(
                    "Serialization type check failed for ${target.name}; Reflected serializer type mismatch (Expected ${type.name}, got ${
                        ReflectType.values()[buf.readByte().toInt()].name
                    })"
                )
            if (buf.readInt() != preCalculatedHash)
                throw IllegalArgumentException("Packet checksum failed for ${target.name}; Packet order mismatch")
            val constructedMap = mutableMapOf<DoubleHashedType, Any?>()
            target.declaredFields.forEachIndexed { i, _ ->
                val name = buf.deserializeAs<DoubleHashedType>()
                if (buf.readBoolean()) {
                    constructedMap[name] = null
                } else {
                    constructedMap[name] = serializers[i].deserialize(buf)
                }
            }
            return when (type) {
                ReflectType.CONSTRUCTOR_INJECTION -> performConstructorInjection(constructedMap)
                ReflectType.FIELD_INJECTION -> performFieldInjection(map = constructedMap)
                ReflectType.MIXED_INJECTION -> performMixedInjection(constructedMap)
            }
        }

        private fun performConstructorInjection(map: Map<DoubleHashedType, Any?>): T {
            // Dynamic constructor injection
            val parameters =
                targetConstructor.kotlinFunction!!.parameters.map { map[DoubleHashedType(it.name!!)] }.toTypedArray()
            println("Map: $map")
            println("Parameters: ${parameters.map { if(it is Array) (it as kotlin.Array<out Any>).contentToString() else it.toString() }}")
            return targetConstructor.newInstance(*parameters) as T
        }

        private fun performFieldInjection(
            instance: Any = targetConstructor.newInstance(),
            map: Map<DoubleHashedType, Any?>
        ): T {
            map.forEach { (key, value) ->
                fieldMap[key]?.apply {
                    isAccessible = true
                    if (Modifier.isFinal(this.modifiers)) {
                        return@forEach
                    }
                    set(instance, value)
                }
            }
            return instance as T
        }

        private fun performMixedInjection(map: Map<DoubleHashedType, Any?>): T {
            val instance = performConstructorInjection(map)
            return performFieldInjection(instance, map)
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ConstructedSerializer<*>

            if (target != other.target) return false
            return serializers == other.serializers
        }

        fun calculateHashCode(): Int {
            var result = target.name.hashCode()
            result = 31 * result + serializers.hashCode()
            return result
        }

        override fun hashCode(): Int {
            return preCalculatedHash
        }


    }
}