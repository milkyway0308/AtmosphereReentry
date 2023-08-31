package skywolf46.atmospherereentry.packetbridge.serializers

import arrow.core.Option
import skywolf46.atmospherereentry.api.packetbridge.DataSerializerBase
import skywolf46.atmospherereentry.api.packetbridge.DataSerializerRegistry
import skywolf46.atmospherereentry.api.packetbridge.data.DoubleHashedType
import skywolf46.atmospherereentry.packetbridge.util.logError

class SerializerRegistryImpl : DataSerializerRegistry {
    private val serializerMap = mutableMapOf<DoubleHashedType, Pair<Class<*>, Lazy<DataSerializerBase<Any>>>>()

    override fun registerSerializer(cls: Class<out Any>, serializerBase: Lazy<DataSerializerBase<out Any>>): Boolean {
        serializerMap[DoubleHashedType(cls)] = cls to serializerBase as Lazy<DataSerializerBase<Any>>
        return true
    }

    override fun registerSerializer(cls: Class<out Any>, serializerBase: DataSerializerBase<out Any>): Boolean {
        serializerMap[DoubleHashedType(cls)] = cls to lazyOf(serializerBase) as Lazy<DataSerializerBase<Any>>
        return true
    }

    override fun acquireSerializer(cls: Class<out Any>): Option<DataSerializerBase<Any>> {
        return Option.fromNullable(serializerMap[DoubleHashedType(cls)]?.second?.value)
    }

    override fun acquireSerializer(hash: DoubleHashedType): Option<DataSerializerBase<Any>> {
        return Option.fromNullable(serializerMap[hash]?.second?.value)
    }

    override fun wakeLazySerializers() {
        for ((_, v) in serializerMap.toMap()) {
            runCatching {
                v.second.value
            }.onFailure {
                logError("Failed to lazy initialize serializer for ${v.first.name} : ${it.message}")
            }
        }
    }

}